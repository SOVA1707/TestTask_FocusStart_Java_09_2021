package testtask.nikita.ru;

import java.io.*;
import java.util.ArrayList;

public class Main {
    static boolean sort_flag = true; // -a true // -d false
    static boolean type_flag = true; // -s true // -i false
    static int lines_buffer = 15000; // количество строк, которые читает программа за 1 проход.
    static ArrayList<Long> readed_bytes = new ArrayList<>();
    static boolean allready_write = false;
    static String last_element = "";
    static int last_element_integer = 0;
    static ArrayList<String> filenames = new ArrayList<>(); // 0 - out file // 1+ - in file
    static ArrayList<ArrayList<String>> arrays = new ArrayList<>();
    static ArrayList<String> sort_arr = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> arrays_int = new ArrayList<>();
    static ArrayList<Integer> sort_arr_int = new ArrayList<>();

    public static void main(String[] args) {
        //чтение аргументов
        for (String arg : args) {
            if (arg.charAt(0) == '-') {
                switch (arg.charAt(1)) {
                    case 'd':
                        sort_flag = false;
                        break;
                    case 'i':
                        type_flag = false;
                }
            } else {
                filenames.add(arg);
                readed_bytes.add((long) 0);
            }
        }
        //удаляем лишний счётчик считанных байтов
        if (readed_bytes.size() > 0) readed_bytes.remove(0);
        //очищаем файл под запись
        if (filenames.size() > 0) {
            try (BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(filenames.get(0)))) {
                byte[] ar = {};
                bf.write(ar);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create a file \"" + filenames.get(0) + '\"');
            } catch (IOException e) {
                System.out.println("System error");
            }
        } else {
            System.out.println("Write filenames : [output] [in 1] [in 2] ... [in n]");
        }

        //пока входящие файлы не кончились, читаем, сортируем и записываем
        boolean is_repeat = true;
        //в зависимости от флага используем специальные переменные и функции
        if (type_flag) {
            while (is_repeat) {
                for (int i = 1; i < filenames.size(); i++) {
                    arrays.add(readFile(filenames.get(i), i - 1));
                }

                if (sort_flag) {
                    for (ArrayList<String> arr : arrays) {
                        sort_arr = inflow(sort_arr, arr);
                    }
                } else {
                    for (ArrayList<String> arr : arrays) {
                        sort_arr = inflow_d(sort_arr, arr);
                    }
                }


                //получаем последний элемент и удаляем его, чтобы далее добавить в начало следующего цикла
                last_element = sort_arr.get(sort_arr.size() - 1);
                sort_arr.remove(sort_arr.size() - 1);

                //если в аргументах не написано имя файла
                if (filenames.size() > 0) {
                    write_file(filenames.get(0), sort_arr);
                } else {
                    System.out.println("Write filenames : [output] [in_1] [in_2] ... [in_n]");
                }

                sort_arr.clear();
                arrays.clear();

                is_repeat = false;
                for (Long n : readed_bytes) {
                    if (n > -1) is_repeat = true;
                }
            }
            //когда кончились входящие файлы, записываем последний элемент в конец
            sort_arr.add(last_element);
            write_file(filenames.get(0), sort_arr);
        }else {
            //то же самое для целочисленных
            while (is_repeat) {
                for (int i = 1; i < filenames.size(); i++) {
                    arrays_int.add(readFile_int(filenames.get(i), i - 1));
                }

                if (sort_flag) {
                    for (ArrayList<Integer> arr : arrays_int) {
                        sort_arr_int = inflow_int(sort_arr_int, arr);
                    }
                } else {
                    for (ArrayList<Integer> arr : arrays_int) {
                        sort_arr_int = inflow_d_int(sort_arr_int, arr);
                    }
                }


                //получаем последний элемент и удаляем его, чтобы далее добавить в начало следующего цикла
                last_element_integer = sort_arr_int.get(sort_arr_int.size() - 1);
                sort_arr_int.remove(sort_arr_int.size() - 1);

                if (filenames.size() > 0) {
                    write_file_int(filenames.get(0), sort_arr_int);
                } else {
                    System.out.println("Write filenames : [output] [in_1] [in_2] ... [in_n]");
                }

                sort_arr_int.clear();
                arrays_int.clear();

                is_repeat = false;
                for (Long n : readed_bytes) {
                    if (n > -1) is_repeat = true;
                }
            }
            sort_arr_int.add(last_element_integer);
            write_file_int(filenames.get(0), sort_arr_int);
        }
    }

    //читаем файл по [lines_buffer] строк, возвращаем массив
    static ArrayList<String> readFile(String filename, int index) {
        ArrayList<String> text = new ArrayList<>();
        Long read_bytes = readed_bytes.get(index);
        if (read_bytes == -1) return text;
        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "\\" + filename))) {
            int c;
            String s = "";
            bf.skip(read_bytes);
            int i = 0;
            while ((c = bf.read()) != -1) {
                read_bytes++;
                if (c == '\n') {
                    i++;
                    text.add(s);

                    if (i > lines_buffer) {
                        readed_bytes.set(index, read_bytes);
                        break;
                    }

                    s = "";
                    continue;
                }
                s += (char) c;
            }
            if (c == -1) {
                readed_bytes.set(index, (long) -1);
            }
            text.add(s);
        } catch (FileNotFoundException e) {
            System.out.println("File \"" + filename + "\" not found.");
        } catch (IOException e) {
            System.out.println("System error");
        }
        return text;
    }

    //читаем файл по [lines_buffer] строк, возвращаем массив
    static ArrayList<Integer> readFile_int(String filename, int index) {
        ArrayList<Integer> numbers = new ArrayList<>();
        Long read_bytes = readed_bytes.get(index);
        if (read_bytes == -1) return numbers;
        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "\\" + filename))) {
            int c;
            String s = "";
            bf.skip(read_bytes);
            int i = 0;
            while ((c = bf.read()) != -1) {
                read_bytes++;
                if (c == '\n') {
                    //проверка на недопустимый ввод
                    try {
                        numbers.add(Integer.parseInt(s));
                    }catch (java.lang.NumberFormatException e) {
                        System.out.println("Присутствуют недопустимые символы в строке:" + s);
                    }

                    i++;
                    if (i > lines_buffer) {
                        readed_bytes.set(index, read_bytes);
                        break;
                    }

                    s = "";
                    continue;
                }
                    if (c != 13) s += (char) c;
            }
            if (c == -1) {
                readed_bytes.set(index, (long) -1);
            }
            try {
                numbers.add(Integer.parseInt(s));
            }catch (java.lang.NumberFormatException e) {
                System.out.println("Присутствуют недопустимые символы в строке:" + s);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File \"" + filename + "\" not found.");
        } catch (IOException e) {
            System.out.println("System error");
        }
        return numbers;
    }

    static void write_file(String filename, ArrayList<String> arr) {
        //записываем в конец файла
        try (FileWriter fw = new FileWriter(filename, true)) {
            for (String str : arr) {
                for (char c : str.toCharArray()) fw.write(c);
                if (str.length() > 0) fw.write('\n');
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot create a file \"" + filename + '\"');
        } catch (IOException e) {
            System.out.println("System error");
        }
        allready_write = true;
    }

    static void write_file_int(String filename, ArrayList<Integer> arr) {
        //записываем в конец файла
        try (FileWriter fw = new FileWriter(filename, true)) {
            for (Integer n : arr) {
                for (char c : n.toString().toCharArray()) fw.write(c);
                if (n.toString().length() > 0) fw.write('\n');
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot create a file \"" + filename + '\"');
        } catch (IOException e) {
            System.out.println("System error");
        }
        allready_write = true;
    }

    //сортировка двух массивов вставкой в один, который возвращаем
    static ArrayList<String> inflow(ArrayList<String> arr1, ArrayList<String> arr2) {

        ArrayList<String> new_arr = new ArrayList<>();
        int k = 0, m = 0;
        int move;
        int count = 0;
        new_arr.add(last_element);

        if (last_element.equals("")) {
            //чтобы цикл начинался с 1
            if (k >= arr1.size() && m >= arr2.size()) {
                return new_arr;
            } else if (k >= arr1.size()) {
                new_arr.add(arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(arr1.get(k));
                k++;
            } else if (arr1.get(k).compareTo(arr2.get(m)) < 0) {
                new_arr.add(arr1.get(k));
                k++;
            } else {
                new_arr.add(arr2.get(m));
                m++;
            }
        }

        for (int i = 1; i < arr1.size() + arr2.size(); i++) {
            if (k >= arr1.size() && m >= arr2.size()) {
                break;
            } else if (k >= arr1.size()) {
                new_arr.add(i, arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else if (arr1.get(k).compareTo(arr2.get(m)) < 0) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else {
                new_arr.add(i, arr2.get(m));
                m++;
            }
            //если попадается неотсортированный элемент, то идём назад по массиву до того места, куда он может встать
            move = 0;
            while (new_arr.get(i - move).compareTo(new_arr.get(i - move - 1)) < 0) {
                String temp = new_arr.get(i - move);
                new_arr.remove(i - move);
                move++;
                if (move >= i) {
                    //если запись в файл уже началась, то читаем файл и вставляем в него элемент
                    if (!allready_write) {
                        new_arr.add(0, temp);
                    } else {
                        String s = temp;
                        Long read_byte = Long.valueOf(0);
                        //поиск места
                        while (s.compareTo(new_arr.get(i - move - 1)) >= 0) {
                            s = "";
                            try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                                int c;
                                bf.skip(read_byte);
                                while ((c = bf.read()) != -1) {
                                    read_byte++;
                                    if (c == '\n') {
                                        break;
                                    }
                                    s += (char) c;
                                }
                            } catch (FileNotFoundException e) {
                                System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                            } catch (IOException e) {
                                System.out.println("System error");
                            }

                        }
                        //запись нового файла с добавленным элементом
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                            try (FileWriter fw = new FileWriter("temp")) {
                                while (read_byte > 0) {
                                    fw.write(bf.read());
                                    read_byte--;
                                }
                                fw.write(s);
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        //запись данных в исходный файл
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream("temp"))) {
                            try (FileWriter fw = new FileWriter(filenames.get(0))) {
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        //удаляем временный файл
                        File temp_file = new File("temp");
                        temp_file.delete();

                        i--;
                        count++;
                    }
                    break;
                } else {
                    new_arr.add(i - move, temp);
                }
            }
        }
        System.out.println("Потеряно элементов: " + count);
        return new_arr;
    }

    static ArrayList<Integer> inflow_int(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {

        ArrayList<Integer> new_arr = new ArrayList<>();
        int k = 0, m = 0;
        int move;
        int count = 0;
        if (last_element_integer != 0) new_arr.add(last_element_integer);

        if (last_element_integer == 0) {
            //чтобы цикл начинался с 1
            if (k >= arr1.size() && m >= arr2.size()) {
                return new_arr;
            } else if (k >= arr1.size()) {
                new_arr.add(arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(arr1.get(k));
                k++;
            } else if (arr1.get(k) < arr2.get(m)) {
                new_arr.add(arr1.get(k));
                k++;
            } else {
                new_arr.add(arr2.get(m));
                m++;
            }
        }

        for (int i = 1; i < arr1.size() + arr2.size(); i++) {
            if (k >= arr1.size() && m >= arr2.size()) {
                break;
            } else if (k >= arr1.size()) {
                new_arr.add(i, arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else if (arr1.get(k) < arr2.get(m)) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else {
                new_arr.add(i, arr2.get(m));
                m++;
            }
            //если попадается неотсортированный элемент, то идём назад по массиву до того места, куда он может встать
            move = 0;
            while (new_arr.get(i - move) < new_arr.get(i - move - 1)) {
                int temp = new_arr.get(i - move);
                new_arr.remove(i - move);
                move++;
                if (move >= i) {
                    //если запись в файл уже началась, то читаем файл и вставляем в него элемент
                    if (!allready_write) {
                        new_arr.add(0, temp);
                    } else {
                        int s = temp;
                        Long read_byte = 0L;
                        //поиск места
                        while (s >= new_arr.get(i - move - 1)) {
                            s = 0;
                            try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                                int c;
                                String number = "";
                                bf.skip(read_byte);
                                while ((c = bf.read()) != -1) {
                                    read_byte++;
                                    if (c == '\n') {
                                        break;
                                    }
                                    number += (char) c;
                                }
                                try {
                                    s = Integer.parseInt(number);
                                }catch (java.lang.NumberFormatException e) {
                                    System.out.println("Присутствуют недопустимые символы в строке:" + number);
                                }
                            } catch (FileNotFoundException e) {
                                System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                            } catch (IOException e) {
                                System.out.println("System error");
                            }

                        }
                        //запись нового файла с добавленным элементом
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                            try (FileWriter fw = new FileWriter("temp")) {
                                while (read_byte > 0) {
                                    fw.write(bf.read());
                                    read_byte--;
                                }
                                fw.write(String.valueOf(s));
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        //запись данных в исходный файл
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream("temp"))) {
                            try (FileWriter fw = new FileWriter(filenames.get(0))) {
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        new File("temp").delete();

                        i--;
                        count++;
                    }
                    break;
                } else {
                    new_arr.add(i - move, temp);
                }
            }
        }
        System.out.println("Потеряно элементов: " + count);
        return new_arr;
    }

    static ArrayList<Integer> inflow_d_int(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {

        ArrayList<Integer> new_arr = new ArrayList<>();
        int k = 0, m = 0;
        int move;
        int count = 0;
        if (last_element_integer != 0) new_arr.add(last_element_integer);

        if (last_element_integer == 0) {
            //чтобы цикл начинался с 1
            if (k >= arr1.size() && m >= arr2.size()) {
                return new_arr;
            } else if (k >= arr1.size()) {
                new_arr.add(arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(arr1.get(k));
                k++;
            } else if (arr1.get(k) > arr2.get(m)) {
                new_arr.add(arr1.get(k));
                k++;
            } else {
                new_arr.add(arr2.get(m));
                m++;
            }
        }

        for (int i = 1; i < arr1.size() + arr2.size(); i++) {
            if (k >= arr1.size() && m >= arr2.size()) {
                break;
            } else if (k >= arr1.size()) {
                new_arr.add(i, arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else if (arr1.get(k) > arr2.get(m)) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else {
                new_arr.add(i, arr2.get(m));
                m++;
            }
            //если попадается неотсортированный элемент, то идём назад по массиву до того места, куда он может встать
            move = 0;
            while (new_arr.get(i - move) > new_arr.get(i - move - 1)) {
                int temp = new_arr.get(i - move);
                new_arr.remove(i - move);
                move++;
                if (move >= i) {
                    //если запись в файл уже началась, то читаем файл и вставляем в него элемент
                    if (!allready_write) {
                        new_arr.add(0, temp);
                    } else {
                        int s = temp;
                        Long read_byte = 0L;
                        //поиск места
                        while (s <= new_arr.get(i - move - 1)) {
                            s = 0;
                            try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                                int c;
                                String number = "";
                                bf.skip(read_byte);
                                while ((c = bf.read()) != -1) {
                                    read_byte++;
                                    if (c == '\n') {
                                        break;
                                    }
                                    number += (char) c;
                                }
                                try {
                                    s = Integer.parseInt(number);
                                }catch (java.lang.NumberFormatException e) {
                                    System.out.println("Присутствуют недопустимые символы в строке:" + number);
                                }
                            } catch (FileNotFoundException e) {
                                System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                            } catch (IOException e) {
                                System.out.println("System error");
                            }

                        }
                        //запись нового файла с добавленным элементом
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                            try (FileWriter fw = new FileWriter("temp")) {
                                while (read_byte > 0) {
                                    fw.write(bf.read());
                                    read_byte--;
                                }
                                fw.write(String.valueOf(s));
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        //запись данных в исходный файл
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream("temp"))) {
                            try (FileWriter fw = new FileWriter(filenames.get(0))) {
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        new File("temp").delete();

                        i--;
                        count++;
                    }
                    break;
                } else {
                    new_arr.add(i - move, temp);
                }
            }
        }
        System.out.println("Потеряно элементов: " + count);
        return new_arr;
    }

    static ArrayList<String> inflow_d(ArrayList<String> arr1, ArrayList<String> arr2) {

        ArrayList<String> new_arr = new ArrayList<>();
        int k = 0, m = 0;
        int move;
        int count = 0;
        new_arr.add(last_element);

        if (last_element.equals("")) {
            //чтобы цикл начинался с 1
            if (k >= arr1.size() && m >= arr2.size()) {
                return new_arr;
            } else if (k >= arr1.size()) {
                new_arr.add(arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(arr1.get(k));
                k++;
            } else if (arr1.get(k).compareTo(arr2.get(m)) > 0) {
                new_arr.add(arr1.get(k));
                k++;
            } else {
                new_arr.add(arr2.get(m));
                m++;
            }
        }

        for (int i = 1; i < arr1.size() + arr2.size(); i++) {
            if (k >= arr1.size() && m >= arr2.size()) {
                break;
            } else if (k >= arr1.size()) {
                new_arr.add(i, arr2.get(m));
                m++;
            } else if (m >= arr2.size()) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else if (arr1.get(k).compareTo(arr2.get(m)) > 0) {
                new_arr.add(i, arr1.get(k));
                k++;
            } else {
                new_arr.add(i, arr2.get(m));
                m++;
            }
            //если попадается неотсортированный элемент, то идём назад по массиву до того места, куда он может встать
            move = 0;
            while (new_arr.get(i - move).compareTo(new_arr.get(i - move - 1)) > 0) {
                String temp = new_arr.get(i - move);
                new_arr.remove(i - move);
                move++;
                if (move >= i) {
                    //если запись в файл уже началась, то читаем файл и вставляем в него элемент
                    if (!allready_write) {
                        new_arr.add(0, temp);
                    } else {
                        String s = temp;
                        Long read_byte = Long.valueOf(0);
                        //поиск места
                        while (s.compareTo(new_arr.get(i - move - 1)) <= 0) {
                            s = "";
                            try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                                int c;
                                bf.skip(read_byte);
                                while ((c = bf.read()) != -1) {
                                    read_byte++;
                                    if (c == '\n') {
                                        break;
                                    }
                                    s += (char) c;
                                }
                                if (c == -1) {
                                    new_arr.add(0, temp);
                                }

                            } catch (FileNotFoundException e) {
                                System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                            } catch (IOException e) {
                                System.out.println("System error");
                            }

                        }
                        //запись нового файла с добавленным элементом
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream(filenames.get(0)))) {
                            try (FileWriter fw = new FileWriter("temp")) {
                                while (read_byte > 0) {
                                    fw.write(bf.read());
                                    read_byte--;
                                }
                                fw.write(s);
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        //запись данных в исходный файл
                        try (BufferedInputStream bf = new BufferedInputStream(new FileInputStream("temp"))) {
                            try (FileWriter fw = new FileWriter(filenames.get(0))) {
                                int c;
                                while ((c = bf.read()) != -1) fw.write((byte) c);
                            } catch (FileNotFoundException e) {
                                System.out.println("File \"" + filenames.get(0) + "\" not found");
                            } catch (IOException e) {
                                System.out.println("System error");
                            }
                        } catch (FileNotFoundException e) {
                            System.out.println("Cannot create file \"" + filenames.get(0) + '\"');
                        } catch (IOException e) {
                            System.out.println("System error");
                        }
                        File temp_file = new File("temp");
                        temp_file.delete();

                        i--;
                        count++;
                    }
                    break;
                } else {
                    new_arr.add(i - move, temp);
                }
            }
        }
        System.out.println("Потеряно элементов: " + count);
        return new_arr;
    }

}
