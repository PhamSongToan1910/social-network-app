package com.tunghq.fsocialmobileapp.Util;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class DigitalSignatureUtil {

    public static String signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] digitalSignature = signature.sign();
        return Base64.encodeToString(digitalSignature, Base64.DEFAULT);
    }

    // Xác thực chữ ký số
    public static boolean verifySignature(byte[] data, String receivedSignature, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        byte[] decodedSignature = Base64.decode(receivedSignature, Base64.DEFAULT);
        return signature.verify(decodedSignature);
    }

    public static void sendFileWithSignature(File file, PrivateKey privateKey, PublicKey publicKey, String myUid, String id, String filename) throws Exception {
        // Đọc nội dung file
        byte[] fileData = readFileBytes(file);
        System.out.println("filedata:  " + fileData);

        // Tạo chữ ký số
        String digitalSignature = DigitalSignatureUtil.signData(fileData, privateKey);

        // Gửi file, chữ ký số và public key qua Firebase
        uploadFileToFirebase(fileData, digitalSignature, publicKey, myUid, id, filename);
    }

    private static void uploadFileToFirebase(byte[] fileData, String digitalSignature, PublicKey publicKey, String myUid, String id, String filename) {
        // Firebase storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SignedFiles/file_" + timeStamp;

        // Lưu file lên Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileName);
        storageReference.putBytes(fileData)
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL tải xuống của file
                    taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                // URL thành công
                                String fileUrl = downloadUri.toString();
                                Log.i("File Upload", "File URL: " + fileUrl);
                                // Lưu thông tin chữ ký và khóa public vào Firebase Database
                                saveSignatureAndKeyToFirebase(fileUrl, digitalSignature, publicKey, myUid, id, filename);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("File Upload", "Failed to get download URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("File Upload", "Error: " + e.getMessage());
                });
    }


    private static void saveSignatureAndKeyToFirebase(String fileUrl, String digitalSignature, PublicKey publicKey, String myUid, String id, String filename) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("message", fileUrl + "\\+" + filename);
        fileInfo.put("signature", digitalSignature);
        fileInfo.put("publicKey", Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
        fileInfo.put("timestamp", System.currentTimeMillis());
        fileInfo.put("sender", myUid);
        fileInfo.put("receiver", id);
        fileInfo.put("type", "file");

        databaseReference.child("Chats").push().setValue(fileInfo);

        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(myUid)
                .child(id);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef1.child("chatListId").setValue(id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(id)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef2.child("chatListId").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void receiveAndVerifyFile(String fileUrl, String receivedSignature, String encodedPublicKey, VerificationCallback callback) {
        // Firebase storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        storageReference.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(fileData -> {
                    try {
                        // Giải mã public key
                        PublicKey publicKey = KeyFactory.getInstance("RSA")
                                .generatePublic(new X509EncodedKeySpec(Base64.decode(encodedPublicKey, Base64.DEFAULT)));

                        // Xác thực chữ ký số
                        boolean isValid = verifySignature(fileData, receivedSignature, publicKey);

                        if (isValid) {
                            Log.i("Verification", "File is authentic and unaltered!");
                        } else {
                            Log.e("Verification", "File is tampered or signature is invalid!");
                        }
                        callback.onVerificationComplete(isValid);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onVerificationComplete(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("File Download", "Error: " + e.getMessage());
                    callback.onVerificationComplete(false);  // Trả về false nếu tải file thất bại
                });
    }

    public static byte[] readFileBytes(File file) {
        try{
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024]; // Kích thước buffer (có thể thay đổi nếu cần)
            int bytesRead;

            // Đọc file từng phần và ghi vào ByteArrayOutputStream
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            // Đóng stream và trả về mảng byte
            fis.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

    public interface VerificationCallback {
        void onVerificationComplete(boolean isValid);
    }
}

