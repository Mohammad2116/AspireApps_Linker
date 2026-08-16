package ir.aspireapps.linker.linksservice.converter;

import ir.aspireapps.linker.linksservice.model.Link;
import org.springframework.stereotype.Component;

@Component
public class LinkConverter {
    private static final String CHARACTERS = "KLMNOPQRSTUVWXYZmnopqrstuvwxyz0123456789ABCDEFGHIJabcdefghijkl";
    private static final int BASE = 62;

    public void encode(Link link) {
        long value = link.getId();
        StringBuilder sb = new StringBuilder();
        if (value == 0) {
            link.setShortUrl(String.valueOf(CHARACTERS.charAt(0)));
        }
        while (value > 0) {
            sb.append(
                    CHARACTERS.charAt(
                            (int) (value % BASE)
                    )
            );
            value /= BASE;
        }
        link.setShortUrl(sb.reverse().toString());
    }

    public long decode(String shorted) {
        long result = 0;
        for (int i = 0; i < shorted.length(); i++) {
            char c = shorted.charAt(i);
            int value = CHARACTERS.indexOf(c);
            if (value == -1)
                throw new RuntimeException("Invalid shorted link: " + shorted);
            result = result * BASE + value;
        }
        return result;
    }
}
