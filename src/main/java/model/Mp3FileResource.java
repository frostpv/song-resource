package model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ByteArrayResource;

@Getter
@Setter
public class Mp3FileResource {
    ByteArrayResource file;
    String filename;
}
