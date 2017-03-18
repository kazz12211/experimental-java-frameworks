package workflow.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import ariba.ui.aribaweb.util.AWGenericException;

public class MailUtil {

    public static byte[] bytesForMime (MimeMessage message)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            message.writeTo(os);
        } catch (IOException e) {
            throw new AWGenericException(e);
        } catch (MessagingException e) {
            throw new AWGenericException(e);
        }
        return os.toByteArray();        
    }

}
