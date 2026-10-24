package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

/**
 * Utility class to build responsive, accessible HTML email templates for OTP verification.
 */
public final class OtpEmailTemplateBuilder {

    private OtpEmailTemplateBuilder() {
    }

    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Código de Verificación - ShiftIQ</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1E293B;">
                <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #F8FAFC; padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 560px; background-color: #FFFFFF; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05); overflow: hidden; border: 1px solid #E2E8F0;">
                                <!-- Header -->
                                <tr>
                                    <td style="background-color: #0F172A; padding: 28px 32px; text-align: center;">
                                        <h1 style="margin: 0; color: #FFFFFF; font-size: 22px; font-weight: 700; letter-spacing: 0.5px;">ShiftIQ Platform</h1>
                                        <p style="margin: 4px 0 0 0; color: #94A3B8; font-size: 13px;">Gestión Inteligente de Talleres</p>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding: 36px 32px;">
                                        <h2 style="margin: 0 0 16px 0; color: #0F172A; font-size: 18px; font-weight: 600;">Verificación de Correo Electrónico</h2>
                                        <p style="margin: 0 0 24px 0; font-size: 14px; line-height: 1.6; color: #475569;">
                                            Hemos recibido una solicitud para verificar tu dirección de correo electrónico en ShiftIQ Platform. Usa el siguiente código de seguridad de un solo uso (OTP) para continuar:
                                        </p>
                                        
                                        <!-- OTP Code Box -->
                                        <div style="margin: 28px 0; text-align: center;">
                                            <div style="display: inline-block; background-color: #F1F5F9; border: 2px dashed #CBD5E1; border-radius: 8px; padding: 16px 36px;">
                                                <span style="font-family: 'Courier New', Courier, monospace; font-size: 34px; font-weight: 800; letter-spacing: 8px; color: #0284C7;">{{OTP_CODE}}</span>
                                            </div>
                                        </div>

                                        <p style="margin: 0 0 12px 0; font-size: 13px; line-height: 1.5; color: #64748B;">
                                            Este código expirará en <strong>{{EXPIRATION_MINUTES}} minutos</strong>.
                                        </p>
                                        <p style="margin: 0; font-size: 13px; line-height: 1.5; color: #94A3B8;">
                                            Si tú no realizaste esta solicitud, puedes ignorar este mensaje de forma segura. Nunca compartas este código con nadie.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #F8FAFC; padding: 20px 32px; border-top: 1px solid #E2E8F0; text-align: center;">
                                        <p style="margin: 0; font-size: 12px; color: #94A3B8;">
                                            © 2026 ShiftIQ Platform. Todos los derechos reservados.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;

    /**
     * Builds the HTML content for an OTP verification email.
     *
     * @param otpCode          the 6-digit verification code
     * @param expirationMinutes validity period in minutes
     * @return responsive HTML email string
     */
    public static String buildOtpEmailHtml(String otpCode, int expirationMinutes) {
        return TEMPLATE
                .replace("{{OTP_CODE}}", otpCode != null ? otpCode : "")
                .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
    }
}
