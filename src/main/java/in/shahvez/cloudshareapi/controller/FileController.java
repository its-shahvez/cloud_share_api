package in.shahvez.cloudshareapi.controller;


import in.shahvez.cloudshareapi.document.CreditDetails;
import in.shahvez.cloudshareapi.dto.FileMetadataDto;
import in.shahvez.cloudshareapi.service.FileMetadataService;
import in.shahvez.cloudshareapi.service.UserCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileMetadataService fileMetadataService;
    private final UserCreditService userCreditService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("files") MultipartFile files[]) throws IOException {
        Map<String, Object> response = new HashMap<>();
       List<FileMetadataDto> list = fileMetadataService.uploadFiles(files);

      CreditDetails finalCredits = userCreditService.getCrediteDetails();

       response.put("files",list);
       response.put("remainingCredits",finalCredits.getCredits());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser(){
       List<FileMetadataDto> files= fileMetadataService.getFiles();
       return ResponseEntity.ok(files);

    }
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
       FileMetadataDto file = fileMetadataService.getPublicFile(id);
       return ResponseEntity.ok(file);
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download (@PathVariable String id) throws IOException {
       FileMetadataDto downloadable = fileMetadataService.getDownloadableFile(id);
        Path path= Paths.get(downloadable.getFileLocation());
      Resource resource=  new UrlResource(path.toUri());


      return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .header(HttpHeaders.CONTENT_DISPOSITION, "attechment;filename=\""+downloadable.getName()+"\"")
              .body(resource);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile( @PathVariable String id){
        fileMetadataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id){
       FileMetadataDto file = fileMetadataService.togglePublic(id);
       return ResponseEntity.ok(file);

    }

}
