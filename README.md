### NewgenSoft DXL File Translator

This application is a helper for ingesting Domino DXL files based on specialized file importing
options.  It is designed to work with content included in DXL files and parse content to integrate
into the NewGen Application.


Test using either :   http://localhost:8080/index

or the testing configurations available in: src/test/DXL Direct request.http

##  Initial Configuration
Set the following properties in the application.properties

- spring.servlet.multipart.max-file-size=10MB
- spring.servlet.multipart.max-request-size=20MB

- initial.dxl.upload=C:\\upload\\
- initial.dxl.directory=C:/dxl
- initial.dxl.upload.directory=c:/upload/dxl
- initial.dxl.pattern=.*\.dxl
- initial.dxl.output.prefix=C://upload//dxl//output//
- initial.attachments.path=data//attachments//00//000//
- initial.pdf.path=data//pdf//00//000//
#   n e w g e n - d x l - t r a n s l a t o r 
 
 
