package com.meili.processor.test;

import java.io.*;

/**
 * Author： fanyafeng
 * Date： 2019/3/1 6:19 PM
 * Email: fanyafeng@live.cn
 */
public class PrintTxt {

    public static void printLog(String log) {
        try {
            File file = new File("print.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(log + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
