package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.transfer.ExportedFile;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Where the JavaFX menu writes the files an exporter returns (LO-349). The first file goes to the file the user
 * chose, with the exporter's extension appended when missing. Every other file is a sibling in the same folder:
 * when it shares the first file's base name ({@code theme5.csv} / {@code theme5.vvt}) it takes the chosen base
 * name, otherwise it keeps its own fixed name ({@code vvt.vvt} next to a Theme 6 {@code .txt}). The caller asks
 * before overwriting any existing target other than the chosen file ({@link #toConfirm}). No UI here.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public final class ExportTargets {

    /**
     * @param file      where to write
     * @param content   the exported file
     * @param fixedName true when the name does not follow the user's choice (ask before overwriting)
     */
    public record Target(File file, ExportedFile content, boolean fixedName) {
    }

    private ExportTargets() {
    }

    /**
     * @param chosen file picked in the save dialog
     * @param files  exporter result, at least one
     * @return one target per exported file, in the same order
     */
    public static List<Target> resolve(File chosen, List<ExportedFile> files) {
        ExportedFile primary = files.get(0);
        File primaryFile = withExtension(chosen, FilenameUtils.getExtension(primary.name()));
        String chosenBase = FilenameUtils.removeExtension(primaryFile.getName());
        String primaryBase = FilenameUtils.removeExtension(primary.name());
        List<Target> targets = new ArrayList<>();
        targets.add(new Target(primaryFile, primary, false));
        for (ExportedFile sibling : files.subList(1, files.size())) {
            boolean paired = primaryBase.equals(FilenameUtils.removeExtension(sibling.name()));
            String name = paired ? chosenBase + "." + FilenameUtils.getExtension(sibling.name()) : sibling.name();
            targets.add(new Target(new File(primaryFile.getParentFile(), name), sibling, !paired));
        }
        return targets;
    }

    /**
     * @param chosen  file picked in the save dialog, which already confirmed overwriting it
     * @param targets resolved targets
     * @return every target that already exists on disk, except the chosen file itself: fixed-name and paired
     * siblings, and the primary when its extension was appended
     */
    public static List<File> toConfirm(File chosen, List<Target> targets) {
        File confirmed = chosen.getAbsoluteFile();
        return targets.stream().map(Target::file)
                .filter(f -> !f.getAbsoluteFile().equals(confirmed))
                .filter(File::exists)
                .toList();
    }

    /**
     * Writes every target's bytes as they are.
     *
     * @throws IOException on the first file that cannot be written
     */
    public static void write(List<Target> targets) throws IOException {
        for (Target target : targets) {
            Files.write(target.file().toPath(), target.content().content());
        }
    }

    private static File withExtension(File file, String extension) {
        if (extension.isEmpty()
                || FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT).equals(extension.toLowerCase(Locale.ROOT))) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + "." + extension);
    }
}
