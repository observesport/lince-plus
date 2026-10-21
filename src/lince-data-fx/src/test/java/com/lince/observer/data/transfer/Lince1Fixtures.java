package com.lince.observer.data.transfer;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LegacyToolException;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.transfer.importers.Lince1RegisterImporter;
import com.lince.observer.data.transfer.importers.Lince1ToolImporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Lince 1 fixtures shared by the importer/exporter tests (LO-349): the same {@code Instrumento_prueba.ilince} /
 * {@code Registro_prueba.rlince} pair the {@code integration} export tests use.
 */
public final class Lince1Fixtures {

    public static final String TOOL = "/lince1/import/Instrumento_prueba.ilince";
    public static final String REGISTER = "/lince1/import/Registro_prueba.rlince";

    private Lince1Fixtures() {
    }

    public static File file(String resource) {
        return new File(Lince1Fixtures.class.getResource(resource).getFile());
    }

    public static InputStream stream(String resource) {
        return Lince1Fixtures.class.getResourceAsStream(resource);
    }

    /**
     * The legacy model as the Lince 1 menu loads it: instrument singleton plus the register (also made the
     * singleton). Callers compare exporter output against {@code Registro.exportTo*} on this state.
     */
    public static Registro loadLegacy() {
        try {
            InstrumentoObservacional.loadInstance(file(TOOL));
            List<LegacyToolException> problems = new ArrayList<>();
            Registro registro = Registro.cargarRegistro(file(REGISTER), problems);
            Registro.useInstance(registro);
            return registro;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return legacy criteria that survive the conversion to modern criteria (those with categories), in order
     */
    public static List<Criterio> convertibleCriterios() {
        return Stream.of(InstrumentoObservacional.getInstance().getCriterios())
                .filter(c -> !c.empty() && c.getCategoriasHijo().length > 0)
                .toList();
    }

    /**
     * A modern project built from the fixtures through the Lince 1 importers, with one research profile.
     */
    public static ILinceProject project(LegacyBridge bridge) {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>());
        project.setRegister(new ArrayList<>());
        project.setProfiles(new ArrayList<>(List.of(new ResearchProfile())));
        try (InputStream tool = stream(TOOL); InputStream register = stream(REGISTER)) {
            new Lince1ToolImporter(bridge).importInto(project, tool, ImportScope.FULL, Map.of());
            new Lince1RegisterImporter(bridge).importInto(project, register, ImportScope.FULL, Map.of());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return project;
    }

    public static long tempFiles(String prefix) {
        try (Stream<Path> files = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            return files.filter(p -> p.getFileName().toString().startsWith(prefix)).count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
