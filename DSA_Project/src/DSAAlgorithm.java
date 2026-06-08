import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class DSAAlgorithm {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public void generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        SecureRandom random = new SecureRandom();
        keyGen.initialize(2048, random);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    
    public void savePublicKey(String filePath) throws Exception {
        if (publicKey == null) throw new Exception("Không có Public Key để lưu!");
        byte[] keyBytes = publicKey.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(keyBytes);
        }
    }

    public void savePrivateKey(String filePath) throws Exception {
        if (privateKey == null) throw new Exception("Không có Private Key để lưu!");
        byte[] keyBytes = privateKey.getEncoded();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(keyBytes);
        }
    }

    public void loadPublicKey(String filePath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("DSA");
        this.publicKey = kf.generatePublic(spec);
    }

    public void loadPrivateKey(String filePath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("DSA");
        this.privateKey = kf.generatePrivate(spec);
    }

    public String signText(String data) throws Exception {
        if (privateKey == null) throw new Exception("Vui lòng tạo/tải khóa trước khi ký!");
        Signature dsa = Signature.getInstance("SHA256withDSA");
        dsa.initSign(privateKey);
        dsa.update(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(dsa.sign());
    }

    public boolean verifyText(String data, String signatureBase64) throws Exception {
        if (publicKey == null) throw new Exception("Không tìm thấy Public Key để xác thực!");
        Signature dsa = Signature.getInstance("SHA256withDSA");
        dsa.initVerify(publicKey);
        dsa.update(data.getBytes("UTF-8"));
        byte[] signature = Base64.getDecoder().decode(signatureBase64);
        return dsa.verify(signature);
    }

    public String signFile(File file) throws Exception {
        if (privateKey == null) throw new Exception("Vui lòng tạo/tải khóa trước khi ký!");
        Signature dsa = Signature.getInstance("SHA256withDSA");
        dsa.initSign(privateKey);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192]; // Tăng buffer lên 8KB để đọc file nhanh hơn
            int len;
            while ((len = fis.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }
        }
        return Base64.getEncoder().encodeToString(dsa.sign());
    }

    public boolean verifyFile(File file, String signatureBase64) throws Exception {
        if (publicKey == null) throw new Exception("Không tìm thấy Public Key để xác thực!");
        Signature dsa = Signature.getInstance("SHA256withDSA");
        dsa.initVerify(publicKey);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }
        }
        byte[] signature = Base64.getDecoder().decode(signatureBase64);
        return dsa.verify(signature);
    }
}