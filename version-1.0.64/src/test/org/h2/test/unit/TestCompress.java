/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.unit;

import java.util.Random;

import org.h2.test.TestBase;
import org.h2.tools.CompressTool;

public class TestCompress extends TestBase {
    
    public static void main(String[] a) throws Exception {
        byte[] data = new byte[1000];
        long total = 0;
        for (int i = 100; i < 104; i++) {
            long time = System.currentTimeMillis();
            for (int j = 0; j < 10000000; j++) {
//                System.arraycopy(data, 0, data, 100, 11);
//                for (int k = 0, n = 100; k < 11; k++) {
//                    data[k] = data[n++];
//                }
                
                int outPos = 0, ctrl = 100, len = i;
                
//                do {
//                    switch((len - outPos) & 7) {
//                    case 0:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 7:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 6:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 5:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 4:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 3:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 2:
//                        data[outPos] = data[outPos++ + ctrl];
//                    case 1:
//                        data[outPos] = data[outPos++ + ctrl];
//                    }
//                } while(outPos < len); 
//                
                data[outPos] = data[outPos++ + ctrl];
                data[outPos] = data[outPos++ + ctrl];
                while (outPos < len - 8) {
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                    data[outPos] = data[outPos++ + ctrl];
                }
                while (outPos < len) {
                    data[outPos] = data[outPos++ + ctrl];
                }
                
            }
            // now: 8907/11859, switch: 9078/14782
            long t = (System.currentTimeMillis() - time);
            total += t;
            System.out.println("i: " + i + " time: " + t);
        }
        System.out.println("total: " + total);
    }

    public void test() throws Exception {
        if (config.big) {
            for (int i = 0; i < 100; i++) {
                test(i);
            }
            for (int i = 100; i < 10000; i += (i + i + 1)) {
                test(i);
            }
        } else {
            test(0);
            test(1);
            test(7);
            test(50);
            test(200);
        }
    }

    void test(int len) throws Exception {
        Random r = new Random(len);
        for (int pattern = 0; pattern < 4; pattern++) {
            byte[] buff = new byte[len];
            switch (pattern) {
            case 0:
                // leave empty
                break;
            case 1: {
                for (int x = 0; x < len; x++) {
                    buff[x] = (byte) (x & 10);
                }
                break;
            }
            case 2: {
                r.nextBytes(buff);
                break;
            }
            case 3: {
                for (int x = 0; x < len; x++) {
                    buff[x] = (byte) (x / 10);
                }
                break;
            }
            }
            if (r.nextInt(2) < 1) {
                for (int x = 0; x < len; x++) {
                    if (r.nextInt(20) < 1) {
                        buff[x] = (byte) (r.nextInt(255));
                    }
                }
            }
            String[] algorithm = new String[] { "LZF", "Deflate", "No" };
            CompressTool utils = CompressTool.getInstance();
            for (int i = 0; i < algorithm.length; i++) {
                byte[] out = utils.compress(buff, algorithm[i]);
                byte[] test = utils.expand(out);
                check(test.length, buff.length);
                check(buff, test);
            }
        }
    }

}