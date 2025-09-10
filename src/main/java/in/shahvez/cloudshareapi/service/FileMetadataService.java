package in.shahvez.cloudshareapi.service;

import in.shahvez.cloudshareapi.document.FileMetadataDocument;
import in.shahvez.cloudshareapi.document.ProfileDocument;
import in.shahvez.cloudshareapi.dto.FileMetadataDto;
import in.shahvez.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final ProfileService profileService;
    private final UserCreditService userCreditService;
    private final FileMetadataRepository fileMetadataRepository;

       public List<FileMetadataDto> uploadFiles(MultipartFile files[]) throws IOException {
          ProfileDocument currentProfile =  profileService.getCurrentProfile();
           List<FileMetadataDocument> savedFile = new ArrayList<>();

          if(!userCreditService.hasEnoughCredits(files.length)){
              throw  new RuntimeException("Not Enough credits to upload files: Please Purchase Credits.");
          }

         Path uploadPath =  Paths.get("upload").toAbsolutePath().normalize();
           Files.createDirectories(uploadPath);

          for (MultipartFile file : files){
           String fileName =   UUID.randomUUID()+"."+ StringUtils.getFilenameExtension(file.getOriginalFilename());
          Path targetLoaction =  uploadPath.resolve(fileName);
          Files.copy(file.getInputStream(), targetLoaction, StandardCopyOption.REPLACE_EXISTING);

          FileMetadataDocument fileMetadata = FileMetadataDocument.builder()
                  .fileLocation(targetLoaction.toString())
                  .name(file.getOriginalFilename())
                  .size(file.getSize())
                  .type(file.getContentType())
                  .clerkId(currentProfile.getClerkId())
                  .isPublic(false)
                  .uploadedAt(LocalDate.now())
                  .build();


          userCreditService.consumeCredits();
              savedFile.add( fileMetadataRepository.save(fileMetadata));
          }
        return   savedFile.stream().map(fileMetadataDocument -> mapToDTO(fileMetadataDocument))
                  .collect(Collectors.toList());
       }

    private FileMetadataDto mapToDTO(FileMetadataDocument fileMetadataDocument) {
       return     FileMetadataDto.builder()
                   .id(fileMetadataDocument.getId())
                   .fileLocation(fileMetadataDocument.getFileLocation())
                   .name(fileMetadataDocument.getName())
                   .size(fileMetadataDocument.getSize())
                   .type(fileMetadataDocument.getType())
                   .clerkId(fileMetadataDocument.getClerkId())
                   .isPublic(fileMetadataDocument.getIsPublic())
                   .uploadedAt(fileMetadataDocument.getUploadedAt())
                   .build();

    }

    public List<FileMetadataDto> getFiles(){
          ProfileDocument currentProfile = profileService.getCurrentProfile();
          List<FileMetadataDocument> files =   fileMetadataRepository.findByClerkId(currentProfile.getClerkId());
       return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileMetadataDto getPublicFile(String id){
          Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);

          if(fileOptional.isEmpty() || !fileOptional.get().getIsPublic()){
              throw new RuntimeException("Unable to get the file");
          }else{
          FileMetadataDocument document = fileOptional.get();
          return mapToDTO(document);
          }
    }
    public FileMetadataDto getDownloadableFile(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("file not found"));
        return mapToDTO(file);
    }
    public void deleteFile(String id){
           try{
             ProfileDocument currentFile =  profileService.getCurrentProfile();
             FileMetadataDocument file = fileMetadataRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("file not upload"));
             if (!file.getClerkId().equals(currentFile.getClerkId())){
                 throw new RuntimeException("File not belong to current user");
             }
             Path filePath =Paths.get(file.getFileLocation());
             fileMetadataRepository.deleteById(id);
           Files.deleteIfExists(filePath);
           }catch (Exception e){
               throw new RuntimeException("Error deleting the file");
           }

    }
    public FileMetadataDto togglePublic(String id){
       FileMetadataDocument file =    fileMetadataRepository.findById(id)
                   .orElseThrow(() -> new RuntimeException("File not found"));

       file.setIsPublic(!file.getIsPublic());
       fileMetadataRepository.save(file);
      return mapToDTO(file);
    }

}
