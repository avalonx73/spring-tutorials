package com.springtutorials.generatedocx.controller;

import com.springtutorials.generatedocx.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;


@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generateDocument() throws Exception {
     //   try {
            byte[] document = documentService.generateDocument();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("generated-document.docx")
                            .build()
            );

            var resource = new InputStreamResource(new ByteArrayInputStream(document));

            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "generated-document.docx")
                    .contentType(APPLICATION_OCTET_STREAM)
                    .contentLength(document.length)
                    .body(resource);
/*        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }*/
    }
}
