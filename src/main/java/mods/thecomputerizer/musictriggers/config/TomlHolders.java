package mods.thecomputerizer.musictriggers.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TomlHolders {

    public static List<Type> readCompleteToml(File toml) {
        List<Type> ret = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(toml));
            String trimmedLine = br.readLine().trim();
            int blankLines = 0;
            int tableIndex = 0;
            int actualIndex = 0;
            boolean foundBaseTable = false;
            while (trimmedLine != null) {
                if(foundBaseTable) {
                    if(isBaseTable(trimmedLine)) {
                        if (blankLines > 0) {
                            ret.add(new Blank(new String[]{"" + blankLines}));
                            blankLines = 0;
                        }
                        ret.add(makeTableType(tableIndex, trimmedLine, actualIndex));
                        actualIndex++;
                        tableIndex++;
                    } else if(!trimmedLine.isEmpty() && !isComment(trimmedLine))
                        blankLines = 0;
                }
                if(isComment(trimmedLine)) {
                    if(blankLines>0) {
                        ret.add(new Blank(new String[]{""+blankLines}));
                        blankLines = 0;
                    } ret.add(new Comment(new String[]{trimmedLine},actualIndex));
                    foundBaseTable = false;
                    actualIndex++;
                }
                else if(trimmedLine.isEmpty()) blankLines++;
                else {
                    if(!foundBaseTable) {
                        foundBaseTable = isBaseTable(trimmedLine);
                        if (foundBaseTable) {
                            if (blankLines > 0) {
                                ret.add(new Blank(new String[]{"" + blankLines}));
                                blankLines = 0;
                            }
                            ret.add(makeTableType(tableIndex, trimmedLine, actualIndex));
                            actualIndex++;
                            tableIndex++;
                        }
                    }
                }
                trimmedLine = br.readLine();
                if(trimmedLine!=null) trimmedLine = trimmedLine.trim();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static boolean isBaseTable(String line) {
        return line.startsWith("[") && line.endsWith("]") && !line.contains(".");
    }

    private static Table makeTableType(int index, String line, int actualIndex) {
        return new Table(new String[]{""+index,
                line.replaceAll("\\[", "").replaceAll("]","")},actualIndex);
    }

    private static boolean isComment(String line) {
        return line.startsWith("#");
    }

    public static abstract class Type {
        protected final String[] readIn;
        private int actualIndex;
        protected Type(String[] thing, int index) {
            this.readIn = thing;
            this.actualIndex = index;
        }

        public int getActualIndex() {
            return this.actualIndex;
        }

        /*
            Only do this if something is removed!
        */
        public void updateIndex(int newIndex) {
            this.actualIndex = newIndex;
        }
    }

    public static class Blank extends Type {

        private final int lines;
        protected Blank(String[] thing) {
            super(thing,0);
            this.lines = Integer.parseInt(this.readIn[0]);
        }

        public int getLines() {
            return this.lines;
        }
    }

    public static class Comment extends Type {

        private final String comment;
        protected Comment(String[] thing, int index) {
            super(thing,index);
            this.comment = thing[0];
        }

        public String getComment() {
            return this.comment;
        }
    }

    public static class Table extends Type {

        private int index;
        private final String name;

        public Table(String[] thing, int index) {
            super(thing,index);
            this.index = Integer.parseInt(this.readIn[0]);
            this.name = this.readIn[1];
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

        /*
            Only do this if a table is removed!
        */
        public void updateSongIndex(int newIndex) {
            this.index = newIndex;
        }
    }
}
