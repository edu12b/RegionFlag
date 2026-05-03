package com.eduardo12b.regionflag.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para processar cores HEX (&#RRGGBB) e cores legacy (&a, &b, etc.)
 * em strings, convertendo para Components da Adventure API.
 */
public final class ColorUtil {

    // Padrão para cores HEX: &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");

    // Serializer legacy com '&' como caractere de cor
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexCharacter('#')
                    .hexColors()
                    .build();

    private ColorUtil() {
        // Utilitário - não instanciar
    }

    /**
     * Converte uma string com códigos de cor (HEX e legacy) para um Component.
     *
     * Suporta:
     * - &#FF0000 → cor HEX vermelha
     * - &a, &b, &c etc → cores legacy do Minecraft
     * - &l, &o, &n etc → formatação (negrito, itálico, etc.)
     *
     * @param text Texto com códigos de cor
     * @return Component formatado
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Converte &#RRGGBB para o formato que o LegacyComponentSerializer entende: &#RRGGBB -> &x&R&R&G&G&B&B
        String converted = convertHexCodes(text);
        return LEGACY_SERIALIZER.deserialize(converted);
    }

    /**
     * Converte uma string com códigos de cor para texto plano (sem cores).
     */
    public static String stripColors(String text) {
        if (text == null) return "";
        // Remove &#RRGGBB
        String stripped = HEX_PATTERN.matcher(text).replaceAll("");
        // Remove &X (legacy color codes)
        stripped = stripped.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        return stripped;
    }

    /**
     * Converte &#RRGGBB para o formato &x&R&R&G&G&B&B (usado pelo LegacyComponentSerializer).
     */
    private static String convertHexCodes(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&#");
            replacement.append(hex);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Cria um Component com cor HEX diretamente.
     *
     * @param text  O texto
     * @param hex   Cor HEX (ex: "FF0000" para vermelho)
     * @return Component colorido
     */
    public static Component withHex(String text, String hex) {
        TextColor color = TextColor.fromHexString("#" + hex);
        return Component.text(text).color(color);
    }

    /**
     * Traduz os códigos de cor de uma string (para uso com PlaceholderAPI etc).
     * Retorna a string com os códigos convertidos para o formato § (section sign).
     */
    public static String translateAlternateColorCodes(String text) {
        if (text == null) return "";
        // Converte & para § para cores legacy
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = '\u00A7';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }
}
