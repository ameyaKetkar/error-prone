package com.google.errorprone.bugpatterns.T2R.common;

import com.google.errorprone.bugpatterns.T2R.common.Models.CUsOuterClass.CUs;
import com.google.errorprone.bugpatterns.T2R.common.Models.ProgramOuterClass.Program;
import com.google.errorprone.bugpatterns.T2R.common.Models.RefactorableOuterClass.Refactorable;
import com.google.errorprone.bugpatterns.T2R.common.Models.TFGOuterClass.TFG;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ameya on 1/28/18.
 */
public class RWProtos {

    public static String pckgName = "";

    public static void write(GeneratedMessageV3.Builder builder, String name) {
        write((GeneratedMessageV3) builder.build(), name);
    }

    public static void write(GeneratedMessageV3 msg, String kind) {
        String nameBin = pckgName+kind+"Bin.txt";
        String nameBinSize = pckgName+kind+"BinSize.txt";
        String name = pckgName+kind+".txt";
        try {
            String t = TextFormat.printToString(msg);
            FileOutputStream output1 = new FileOutputStream(name,true);
            output1.write(t.getBytes(Charset.forName("UTF-8")));
            FileOutputStream outputSize = new FileOutputStream(nameBinSize,true);
            String size = msg.getSerializedSize() + " ";
            outputSize.write(size.getBytes(Charset.forName("UTF-8")));
            FileOutputStream output = new FileOutputStream(nameBin,true);
            msg.writeTo(output);
            output.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.  Creating a new file.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeRef(GeneratedMessageV3 msg, String path) {
        String kind = "Ref";
        String nameBin = path+kind+"Bin.txt";
        String nameBinSize = path+kind+"BinSize.txt";
        String name = path+kind+".txt";
        try {
            String t = TextFormat.printToString(msg);
            FileOutputStream output1 = new FileOutputStream(name,true);
            output1.write(t.getBytes(Charset.forName("UTF-8")));
            FileOutputStream outputSize = new FileOutputStream(nameBinSize,true);
            String size = msg.getSerializedSize() + " ";
            outputSize.write(size.getBytes(Charset.forName("UTF-8")));
            FileOutputStream output = new FileOutputStream(nameBin,true);
            msg.writeTo(output);
            output.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.  Creating a new file.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(List<GeneratedMessageV3> builders, String name) {
        builders.forEach(builder-> write(builder,name));
    }

    public static void write(Iterable<GeneratedMessageV3> builders, String name) {
        builders.forEach(builder-> write(builder,name));
    }

    @FunctionalInterface
    public interface TryFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static <T> List<T> read(String what){
        if(what.equals("TFG")){
            return read("TFG", ci -> (T)TFG.parseFrom(ci));
        }else if(what.equals("CU")){
            return read("CU", ci -> (T) CUs.parseFrom(ci));
        }
        else if(what.equals("Ref")){
            return read("Ref", ci -> (T) Refactorable.parseFrom(ci));
        }
        else if(what.equals("Program")){
            return read("Program", ci -> (T) Program.parseFrom(ci));
        }
        return new ArrayList<>();
    }

    public static List<TFG> readTFG(String path){
        try {
            String name = path + "TFG";
            String contents = new String(Files.readAllBytes(Paths.get(name + "BinSize.txt")));
            String[] x = contents.split(" ");
            List<Integer> y = Arrays.asList(x).stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
            InputStream is = new FileInputStream(name + "Bin.txt");
            List<TFG> tfgs = new ArrayList<>();
            for (Integer c : y) {
                byte[] b = new byte[c];
                int i = is.read(b);
                if (i > 0) {
                    CodedInputStream input = CodedInputStream.newInstance(b);
                    tfgs.add(TFG.parseFrom(input));
                }
            }
            return tfgs;
        }
        catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            System.out.println( "TFG protos could not be deserialised");
            return new ArrayList<>();
        }
    }



    public static <T> List<T> read(String name, TryFunction<CodedInputStream,T> parser){
        try {
            name = pckgName + name;
            String contents = new String(Files.readAllBytes(Paths.get(name + "BinSize.txt")));
            String[] x = contents.split(" ");
            List<Integer> y = Arrays.asList(x).stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
            InputStream is = new FileInputStream(name + "Bin.txt");
            List<T> tfgs = new ArrayList<>();
            for (Integer c : y) {
                byte[] b = new byte[c];
                int i = is.read(b);
                if (i > 0) {
                    CodedInputStream input = CodedInputStream.newInstance(b);
                    tfgs.add(parser.apply(input));
                }
            }
            return tfgs;
        }catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            System.out.println( "TFG protos could not be deserialised");
            return new ArrayList<>();
        }
    }


//
//    public static List<TFG> readTFGs(){
//        try {
//            String name = pckgName + "TFG";
//            String contents = new String(Files.readAllBytes(Paths.get(name + "BinSize.txt")));
//            String[] x = contents.split(" ");
//            List<Integer> y = Arrays.asList(x).stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
//            InputStream is = new FileInputStream(name + "Bin.txt");
//            List<TFG> tfgs = new ArrayList<>();
//            for (Integer c : y) {
//                byte[] b = new byte[c];
//                int i = is.read(b);
//                if (i > 0) {
//                    CodedInputStream input = CodedInputStream.newInstance(b);
//                    tfgs.add(TFG.parseFrom(input));
//                }
//            }
//            return tfgs;
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            System.out.println( "TFG protos could not be deserialised");
//            return new ArrayList<>();
//        }
//    }

//    public static List<CUs> readCUs(){
//        try {
//            String name = pckgName + "CU";
//            String contents = new String(Files.readAllBytes(Paths.get(name + "BinSize.txt")));
//            String[] x = contents.split(" ");
//            List<Integer> y = Arrays.asList(x).stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
//            InputStream is = new FileInputStream(name + "Bin.txt");
//            List<CUs> tfgs = new ArrayList<>();
//            for (Integer c : y) {
//                byte[] b = new byte[c];
//                int i = is.read(b);
//                if (i > 0) {
//                    CodedInputStream input = CodedInputStream.newInstance(b);
//                    tfgs.add(CUs.parseFrom(input));
//                }
//            }
//            return tfgs;
//        } catch (Exception e) {
//            System.out.println( "TFG protos could not be deserialised");
//            return new ArrayList<>();
//        }
//    }
//
//
//    public static List<Refactorable> readRef(){
//        try {
//            String name = pckgName + "Ref";
//            String contents = new String(Files.readAllBytes(Paths.get(name + "BinSize.txt")));
//            String[] x = contents.split(" ");
//            List<Integer> y = Arrays.asList(x).stream().map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
//            InputStream is = new FileInputStream(name + "Bin.txt");
//            List<Refactorable> tfgs = new ArrayList<>();
//            for (Integer c : y) {
//                byte[] b = new byte[c];
//                int i = is.read(b);
//                if (i > 0) {
//                    CodedInputStream input = CodedInputStream.newInstance(b);
//                    tfgs.add(Refactorable.parseFrom(input));
//                }
//            }
//            return tfgs;
//        } catch (Exception e) {
//            System.out.println( "Ref protos could not be deserialised");
//            return new ArrayList<>();
//        }
//    }

}
