package APP.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final Path uploadsRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadsRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeMascotaImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de imagen inválido. Solo se permiten JPG/JPEG o PNG");
        }

        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Extensión de imagen inválida. Solo se permiten .jpg, .jpeg o .png");
        }

        // Normalizamos jpeg -> jpg para evitar variantes
        if (ext.equals("jpeg")) {
            ext = "jpg";
        }

        Path mascotasDir = uploadsRoot.resolve("mascotas").normalize();
        try {
            Files.createDirectories(mascotasDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }

        String filename = UUID.randomUUID() + "." + ext;
        Path target = mascotasDir.resolve(filename).normalize();

        // Seguridad: evitar path traversal
        if (!target.startsWith(mascotasDir)) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }

        // Ruta pública servida por Spring (/uploads/**)
        return "/uploads/mascotas/" + filename;
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
