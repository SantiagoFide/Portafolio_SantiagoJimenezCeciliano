/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package TechShop.Santiago.service;


import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FirebaseStorageService {

    @Value("${firebase.bucket.name}")
    private String bucketName;

    @Value("${firebase.storage.path}")
    private String storagePath;

    private final Storage storage;

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    // Sube una imagen a Firebase Storage
    public String uploadImage(MultipartFile localFile, String folder, Integer id) throws IOException {

        String originalName = localFile.getOriginalFilename();
        String fileExtension = "";

        if (originalName != null && originalName.contains(".")) {
            fileExtension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = "img" + getFormattedNumber(id) + fileExtension;

        File tempFile = convertToFile(localFile);

        try {
            return uploadToFirebase(tempFile, folder, fileName);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // Convierte MultipartFile a File temporal
    private File convertToFile(MultipartFile multipartFile) throws IOException {

        File tempFile = File.createTempFile("upload-", ".tmp");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }

        return tempFile;
    }

    // Sube el archivo a Firebase Storage
    private String uploadToFirebase(File file, String folder, String fileName) throws IOException {

        BlobId blobId = BlobId.of(bucketName, storagePath + "/" + folder + "/" + fileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("media")
                .build();

        storage.create(blobInfo, java.nio.file.Files.readAllBytes(file.toPath()));

        return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
    }

    // Formatea el ID con ceros a la izquierda
    private String getFormattedNumber(long id) {
        return String.format("%04d", id);
    }
}