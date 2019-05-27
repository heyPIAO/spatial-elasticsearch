package es.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {

  /**
   * 判断路径是否存在
   */
  public static boolean exist(String path) {
    return Files.exists(Paths.get(path));
  }

  /**
   * 删除路径（文件或目录）
   */
  public static void deletePath(String... paths) throws IOException {
    if (paths != null) {
      for (String path : paths)
        deletePath(new File(path));
    }
  }

  /**
   * 删除路径（文件或目录）
   */
  public static void deletePath(File... paths) throws IOException {
    if (paths != null) {
      for (File path : paths) {
        if (path.exists()) {
          if (path.isFile())
            Files.delete(path.toPath());
          else {
            File[] subPaths = path.listFiles();
            if (subPaths != null)
              for (File subPath : subPaths)
                deletePath(subPath);
            Files.delete(path.toPath());
          }
        }
      }
    }
  }


  /**
   * 重命名文件或目录
   */
  public static boolean rename(String src, String dest) {
    return rename(new File(src), new File(dest));
  }

  /**
   * 重命名文件或目录（文件或目录不存在时会自行创建）
   */
  public static boolean rename(File src, File dest) {
    return src.renameTo(dest);
  }

  /**
   * 覆盖式写入文件
   */
  public static void write(String file, Iterable<String> lines) throws IOException {
    write(Paths.get(file), lines);
  }

  /**
   * 覆盖式写入文件
   */
  public static void write(Path path, Iterable<String> lines) throws IOException {
    Files.createDirectories(path.getParent());
    Files.write(path, lines);
  }

  /**
   * 每一条记录作为一行，向文件追加内容（文件或目录不存在时会自行创建）
   */
  public static void append(String file, Iterable<String> lines) throws IOException {
    append(Paths.get(file), lines);
  }

  /**
   * 每一条记录作为一行，向文件追加内容（文件或目录不存在时会自行创建）
   */
  public static void append(Path path, Iterable<String> lines) throws IOException {
    Files.createDirectories(path.getParent());
    if (!Files.exists(path))
      Files.write(path, lines);
    Files.write(path, lines, StandardOpenOption.APPEND);
  }

  public static List<String> readByLine(String fileOrDir, Integer size, boolean skipFirstLine) throws IOException {
    List<String> lines = new ArrayList<>(size == null ? 10 : size);
    Path dir = Paths.get(fileOrDir);
    if (Files.exists(dir))
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          if (dir.getFileName().toString().startsWith("."))
            return FileVisitResult.SKIP_SUBTREE;
          else
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!file.getFileName().toString().startsWith(".")) {
            try (BufferedReader reader = Files.newBufferedReader(file)) {
              String line;
              if (skipFirstLine)
                reader.readLine();
              while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (size != null && lines.size() >= size)
                  return FileVisitResult.TERMINATE;
              }
            } catch (IOException e) {
              System.out.println("文件`" + file.toString() + "`读取异常");
            }
          }
          return FileVisitResult.CONTINUE;
        }
      });
    return lines;
  }

}
