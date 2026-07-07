package dev.solar.solarelo.gui;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class GuiLayoutHelper {
    public static class LayoutInfo {
        public final int rows;
        public final int limit;
        public final int offset;

        public LayoutInfo(int rows, int limit, int offset) {
            this.rows = rows;
            this.limit = limit;
            this.offset = offset;
        }
    }

    public static LayoutInfo getLayoutInfo(FileConfiguration config, String path, int page) {
        List<String> disposition = config.getStringList(path);
        int rows = 6;
        if (disposition != null && !disposition.isEmpty()) {
            rows = disposition.size();
        } else {
            rows = config.getInt("rows", 6);
        }
        if (rows < 1 || rows > 6) rows = 6;

        int limit = (rows - 1) * 9;
        if (disposition != null && !disposition.isEmpty()) {
            int count = 0;
            for (String row : disposition) {
                for (int c = 0; c < row.length() && c < 9; c++) {
                    if (row.charAt(c) == 'x') {
                        count++;
                    }
                }
            }
            limit = count;
        }
        int offset = (page - 1) * limit;
        return new LayoutInfo(rows, limit, offset);
    }
}
