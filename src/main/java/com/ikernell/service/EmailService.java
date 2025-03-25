package com.ikernell.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void enviarCorreoPregunta(String nombre, String email, String pregunta) throws IOException {
        Email from = new Email(fromEmail, "IKernell Soluciones Software");
        Email to = new Email("alexjanxs@gmail.com");
        String subject = "Nueva pregunta de: " + nombre;

        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <style>" +
                        "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        "        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                        "        .header { background-color: #4a69bd; color: white; padding: 15px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        "        .content { padding: 20px; }" +
                        "        .field { margin-bottom: 15px; }" +
                        "        .label { font-weight: bold; color: #4a69bd; }" +
                        "        .value { padding: 10px; border: 1px solid #eee; border-radius: 5px; background-color: #f9f9f9; }" +
                        "        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <div class='header'>" +
                        "            <h2>Nueva Pregunta Recibida</h2>" +
                        "        </div>" +
                        "        <div class='content'>" +
                        "            <p>Se ha recibido una nueva pregunta desde el formulario de contacto.</p>" +
                        "            <div class='field'>" +
                        "                <div class='label'>Nombre:</div>" +
                        "                <div class='value'>" + nombre + "</div>" +
                        "            </div>" +
                        "            <div class='field'>" +
                        "                <div class='label'>Email:</div>" +
                        "                <div class='value'>" + email + "</div>" +
                        "            </div>" +
                        "            <div class='field'>" +
                        "                <div class='label'>Pregunta:</div>" +
                        "                <div class='value'>" + pregunta + "</div>" +
                        "            </div>" +
                        "        </div>" +
                        "        <div class='footer'>" +
                        "            <p>Este correo fue enviado automáticamente desde el sitio web de IKernell Soluciones Software.</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);
        mail.setReplyTo(new Email(email, nombre));

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new IOException("Error al enviar correo: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Error al enviar correo: " + ex.getMessage(), ex);
        }
    }

    public void enviarCorreoRecuperacion(String emailDestino, String nombreDestino, String enlaceRecuperacion) throws IOException {
        Email from = new Email(fromEmail, "IKernell Soluciones Software");
        Email to = new Email(emailDestino, nombreDestino);
        String subject = "Recuperación de Contraseña - IKernell";

        // Contenido del correo en HTML
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                "        .header { background-color: #4a69bd; color: white; padding: 15px; text-align: center; border-radius: 5px 5px 0 0; }" +
                "        .content { padding: 20px; }" +
                "        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; }" +
                "        .btn { display: inline-block; padding: 10px 20px; background-color: #4a69bd; color: white; text-decoration: none; border-radius: 5px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>Recuperación de Contraseña</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Hola " + nombreDestino + ",</p>" +
                "            <p>Hemos recibido una solicitud para restablecer tu contraseña. Haz clic en el siguiente enlace para continuar:</p>" +
                "            <p><a href='" + enlaceRecuperacion + "' class='btn'>Restablecer Contraseña</a></p>" +
                "            <p>Si no solicitaste este cambio, puedes ignorar este correo.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Este correo fue enviado automáticamente desde el sitio web de IKernell Soluciones Software.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new IOException("Error al enviar correo: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Error al enviar correo: " + ex.getMessage(), ex);
        }
    }
}