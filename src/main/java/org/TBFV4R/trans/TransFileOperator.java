package org.TBFV4R.trans;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransFileOperator {
    public static final String TRANS_WORK_DIR = "resources/trans";
    public static final String SOURCE_CODES_DIR = TRANS_WORK_DIR + "/" + "sourceCodes";
    public static final String ONE_STATIC_MD_CODES_DIR = TRANS_WORK_DIR + "/" + "oneStaticMdCodes";
    public static final String ONE_NORMAL_MD_CODES_DIR = TRANS_WORK_DIR + "/" +"oneNormalMdCodes";
    public static final String MULTI_NORMAL_MD_CODES_DIR = TRANS_WORK_DIR + "/" +"multiNormalMdCodes";
    public static final String MULTI_STATIC_MD_CODES_DIR = TRANS_WORK_DIR + "/" +"multiStaticMdCodes";
    public static final String GENERATED_MAIN_METHOD_DIR = TRANS_WORK_DIR + "/" +"generatedMainMethod";
    public static final String UNKNOWN_CODES_DIR= TRANS_WORK_DIR + "/" +"unknownCodes";
    public static final String ADDED_STATIC_FLAG_DIR = ONE_NORMAL_MD_CODES_DIR + "/" + "addedStaticFlag";
    public final static String ADDED_PRINT_CODES_DIR = TRANS_WORK_DIR + "/" + "addedPrintCodes";
    public final static String HAS_NO_ARRAY_CODES_DIR = TRANS_WORK_DIR + "/" + "hasNoArrayCodes";
    public final static String TRANS_RUNNABLE_DIR = TRANS_WORK_DIR + "/" + "runnable";

    public static void classifySourceCodes() throws IOException {
        int count = 0;
        int num1Static = 0;
        int numMulStatic = 0;
        int num1Normal = 0;
        int numMulNormal = 0;
        int numHasMain = 0;
        int numOther = 0;
        File[] allFiles =  fetchAllJavaFilesInDir(SOURCE_CODES_DIR);
        for (File file : allFiles) {
            System.out.println("Processing the ["+ ++count +"]th program："+file.getName());
            String program = file2String(file.getAbsolutePath());
            String targetPath;

            //mainGENERATED_MAIN_METHOD_DIR，
            if(hasMainMdProgram(program)) {
                targetPath = GENERATED_MAIN_METHOD_DIR + "/" + file.getName();
                numHasMain++;
            }
            //main， ONE_STATIC_MD_CODES_DIR，main
            else if(countStaticMethodProgram(program) == 1 && countNormalMethodProgram(program) == 0){
                //fileoneStaticMdCodes
                targetPath = ONE_STATIC_MD_CODES_DIR + "/" + file.getName();
                num1Static++;
            }
            //main，multiStaticMdCodes
            else if(countStaticMethodProgram(program) > 1 && countNormalMethodProgram(program) == 0){
                targetPath = MULTI_STATIC_MD_CODES_DIR + "/" + file.getName();
                numMulStatic++;
            }
            //main，，oneNormalMdCodes
            else if(countNormalMethodProgram(program) == 1 && countStaticMethodProgram(program) == 0){
                //fileoneNormalMdCodes
                targetPath = ONE_NORMAL_MD_CODES_DIR + "/" + file.getName();
                num1Normal++;
            }
            //main，, multiNormalMdCodes
            else if(countNormalMethodProgram(program) > 1 && countStaticMethodProgram(program) == 0){
                targetPath = MULTI_NORMAL_MD_CODES_DIR + "/" + file.getName();
                numMulNormal++;
            }
            else{
                targetPath = UNKNOWN_CODES_DIR + "/" + file.getName();
                numOther++;
            }
            if(Files.exists(Paths.get(targetPath))){
                Files.delete(Paths.get(targetPath));
            }
            Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(targetPath));
        }
//        System.out.println("" + count + "");
//        System.out.println("main" + numHasMain + "");
//        System.out.println("static" + num1Static + "");
//        System.out.println("static" + numMulStatic + "");
//        System.out.println("" + num1Normal + "");
//        System.out.println("" + numMulNormal + "");
//        System.out.println("" + numOther + "");
    }
    /*
     * @description static， static ， ONE_STATIC_MD_CODES_DIR 
     */
    public static void addStaticFlag4OneNormalMdInDefaultDir() throws IOException {
        int count = 0;
        String addedStaticFlagDir = ADDED_STATIC_FLAG_DIR;
        if(Files.exists(Paths.get(addedStaticFlagDir))) {
            Files.list(Paths.get(addedStaticFlagDir)).forEach(p -> {p.toFile().delete();});
        }else{
            Files.createDirectories(Paths.get(addedStaticFlagDir));
        }
        File[] files = fetchAllJavaFilesInDir(ONE_NORMAL_MD_CODES_DIR);
        for (File file : files) {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(file).getResult().get();
            //static
            cu.findAll(MethodDeclaration.class).stream()
                    .filter(md -> !md.isStatic())
                    .forEach(m -> {
                        NodeList<Modifier> modifiers = m.getModifiers();
                        modifiers.add(Modifier.staticModifier());
                        m.setModifiers(modifiers);
                    });
            // 
            Path outputPath = Paths.get(addedStaticFlagDir, file.getName());
            if(Files.exists(outputPath)){
                Files.delete(outputPath);
            }
            // 
            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(cu.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //ONE_STATIC_MD_CODES_DIR
            Path oneStaticPath = Paths.get(ONE_STATIC_MD_CODES_DIR, file.getName());
            if(Files.exists(oneStaticPath)){
                Files.delete(oneStaticPath);
            }
            Files.copy(outputPath, oneStaticPath);
            count++;
        }
        System.out.println( ""+ count + "static，！" );
    }
    public static String addStaticFlag2SNMP(String snmp){
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(snmp).getResult().get();
        //static
        cu.findAll(MethodDeclaration.class).stream()
                .filter(md -> !md.isStatic())
                .forEach(m -> {
                    NodeList<Modifier> modifiers = m.getModifiers();
                    modifiers.add(Modifier.staticModifier());
                    m.setModifiers(modifiers);
                });
        return cu.toString();
    }
    public static boolean hasMainMdProgram(String program){
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(program).getResult().get();
        return cu.findAll(MethodDeclaration.class).stream()
                .anyMatch(md -> md.getNameAsString().equals("main"));
    }
    public static long countStaticMethodProgram(String program){
        long count;
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(program).getResult().get();
        count = cu.findAll(MethodDeclaration.class).stream()
                .filter(md -> md.isStatic() && !md.getNameAsString().equals("main"))
                .count();
        return count;
    }
    public static long countNormalMethodProgram(String program){
        JavaParser parser = new JavaParser();
        CompilationUnit cu = parser.parse(program).getResult().get();
        long mdCount = cu.findAll(MethodDeclaration.class).stream()
                .filter(md -> !md.isStatic())
                .count();
        return mdCount;
    }
    public static void initTransWorkDir() throws IOException {
        // 
        Files.createDirectories(Path.of(TRANS_WORK_DIR));
        Files.createDirectories(Path.of(SOURCE_CODES_DIR));
        Files.createDirectories(Path.of(ONE_STATIC_MD_CODES_DIR));
        Files.createDirectories(Path.of(ONE_NORMAL_MD_CODES_DIR));
        Files.createDirectories(Path.of(MULTI_NORMAL_MD_CODES_DIR));
        Files.createDirectories(Path.of(MULTI_STATIC_MD_CODES_DIR));
        Files.createDirectories(Path.of(GENERATED_MAIN_METHOD_DIR));
        Files.createDirectories(Path.of(UNKNOWN_CODES_DIR));
        Files.createDirectories(Path.of(ADDED_STATIC_FLAG_DIR));
        Files.createDirectories(Path.of(ADDED_PRINT_CODES_DIR));
        Files.createDirectories(Path.of(TRANS_RUNNABLE_DIR));
    }
    public static void deleteTransWorkDir() throws IOException {
        Files.deleteIfExists(Path.of(TRANS_WORK_DIR));
    }

    public static String file2String(String FilePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(FilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    /*
         .java
     */
    public static File[] fetchAllJavaFilesInDir(String dir) throws IOException {
        Path path = Paths.get(dir);
        List<File> javaFiles = new ArrayList<>();
        if (Files.isDirectory(path)) {
            Files.walk(path)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> javaFiles.add(p.toFile()));
        } else {
            javaFiles.add(path.toFile());
        }
        return javaFiles.toArray(new File[0]);
    }

    /*
     .java
    */
    public static void deleteAllJavaFilesInDir(String dir) throws IOException {
        Path path = Paths.get(dir);
        Files.walk(path)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> p.toFile().delete());
    }
    public static void cleanJavaCodesInTransWorkDir() throws IOException {
        deleteAllJavaFilesInDir(TRANS_WORK_DIR);
    }
    public static void cleanUnusableFilesInTrans() throws IOException {
        deleteDirectoryRecursively(Path.of(GENERATED_MAIN_METHOD_DIR));
        deleteDirectoryRecursively(Paths.get(ONE_NORMAL_MD_CODES_DIR));
    }

    public static void saveRunnablePrograms(String fileName,String program,int n) throws IOException {
        String className = fileName.substring(0, fileName.lastIndexOf("."));
        String dir = TRANS_RUNNABLE_DIR + "/" + className + "/" + String.valueOf(n);
        File dirF =Path.of(dir).toFile();
        if(!dirF.exists()){
            dirF.mkdirs();
        }
        Path path = Path.of(dir + "/" + fileName);
        File file = path.toFile();
        if(file.exists()){
            file.delete();
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(program);
        }
    }
    public static String getAddedPrintCodesOfProgram(String fileName){
        String addedPrintCodesPath = ADDED_PRINT_CODES_DIR + "/" + fileName;
        return file2String(addedPrintCodesPath);
    }

    public static void deleteDirectoryRecursively(Path dir) throws IOException {
        // 
        if (!Files.exists(dir)) {
            return;
        }
        // （：）
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println(": " + path + " - " + e.getMessage());
                    }
                });
    }

    public static void copyPrograms2TransSourceDir(String path) throws IOException {
        if(new File(path).isDirectory()){
            File[] files = fetchAllJavaFilesInDir(path);
            for (File file : files) {
                Path transP = Path.of(SOURCE_CODES_DIR, file.getName());
                if(transP.toFile().exists()){
                   transP.toFile().delete();
                }
                Files.copy(file.toPath(), transP);
            }
        }
        else{
            Path p = Path.of(path);
            Files.copy(p, Path.of(SOURCE_CODES_DIR, p.getFileName().toString()));
        }
    }

    public static boolean paramsContainsArrayType(String programPath){
        String code = file2String(programPath);
        CompilationUnit cu = StaticJavaParser.parse(code);
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : methods) {
            if(isMainMethod(method)){
                continue;
            }
            NodeList<Parameter> parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Type type = parameter.getType();
                System.out.println(type.toString());
                if(type instanceof ArrayType){
                    return true;
                }else if(isCollectionType(type.toString())){
                    return true;
                }
            }
        }
        return false;
    }
    private static boolean isMainMethod(MethodDeclaration method) {
        return method.isPublic() &&
                method.isStatic() &&
                method.getNameAsString().equals("main") &&
                method.getType().toString().equals("void");
    }
    private static boolean isCollectionType(String typeName) {
        return typeName.equals("List") || typeName.equals("ArrayList")
                || typeName.equals("Set") || typeName.equals("HashSet")
                || typeName.equals("Collection") || typeName.equals("LinkedList");
    }
    public static void copyHasNoArrayPrograms() throws IOException {
        File[] files = fetchAllJavaFilesInDir(ADDED_PRINT_CODES_DIR);
        int count = 0;
        int hasNoArrayCount = 0;
        for (File file : files) {
            count++;
            System.out.println("[]"+ count +"："+file.getName());
            if(!paramsContainsArrayType(file.getAbsolutePath())){
                Files.copy(file.toPath(), Paths.get(HAS_NO_ARRAY_CODES_DIR, file.getName()));
                hasNoArrayCount++;
            }
        }
        System.out.println("[]"+ count +"");
        System.out.println("[]"+ hasNoArrayCount + "");
    }
    public static void saveAddedPrintCodes(String code, String path) throws IOException {
        File file = new File(path);
        if(Files.exists(file.toPath())) {
            Files.delete(file.toPath());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(code);
        } catch (IOException e) {
            System.out.println(": " + e.getMessage());
        }
    }

    public static boolean isSSMP(String program){
        if(countStaticMethodProgram(program) == 1){
            return true;
        }
        return false;
    }
    //
    public static boolean isSNMP(String program){
        if(countNormalMethodProgram(program) == 1){
            return true;
        }
        return false;
    }

    public static String trans2SSMP(String pureProgram){
        if(isSSMP(pureProgram)){
            return pureProgram;
        }
        if(isSNMP(pureProgram)){
            String transProgram = addStaticFlag2SNMP(pureProgram);
            if(isSSMP(transProgram)){
                return transProgram;
            }
        }
        System.out.println("SSMP!");
        return null;
    }

}
