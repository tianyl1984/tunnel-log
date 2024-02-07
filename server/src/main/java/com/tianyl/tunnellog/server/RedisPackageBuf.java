package com.tianyl.tunnellog.server;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RedisPackageBuf extends PackageBuf {

    public RedisPackageBuf() {
        super();
    }

    @Override
    public int tryToTake(int cnt) {
        byte[] bs = getDatas(cnt);
        ParseResult result = tryToParse(0, bs);
        if (result == null) {
            return 0;
        }
        return result.useSize;
    }

    @Override
    public String parseData(byte[] data) {
        ParseResult result = tryToParse(0, data);
        if (result == null) {
            return "parse error data";
        }
        if (result.value instanceof List) {
            List<Object> list = (List) result.value;
            String str = list.stream().map(Object::toString).collect(Collectors.joining(" "));
            String date = DateUtil.format(new Date());
            return String.format("%s %s", date, str);
        }
        return "error data type:" + result.value.getClass().getName();
    }

    private static ParseResult tryToParse(int start, byte[] data) {
        byte c = data[start];
        ParseResult result = null;
//        单行字符串（Simple Strings）： 响应的首字节是 "+"
//        错误（Errors）： 响应的首字节是 "-"
//        整型（Integers）： 响应的首字节是 ":"
//        多行字符串（Bulk Strings）： 响应的首字节是"$"
//        数组（Arrays）： 响应的首字节是 "*"
        switch (c) {
            case '+':
            case '-':
                result = parseString(start, data);
                break;
            case ':':
                result = parseInteger(start, data);
                break;
            case '$':
                result = parseString2(start, data);
                break;
            case '*':
                result = parseArray(start, data);
                break;
            default:
                throw new RuntimeException("unknown data type");
        }
        return result;
    }

    private static ParseResult parseArray(int start, byte[] data) {
        if (data.length < 4) {
            return null;
        }
        int pos = findFirstCRLF(start, data);
        if (pos == -1) {
            return null;
        }
        ParseResult result = new ParseResult();
        int arrSize = Integer.parseInt(new String(data, 1 + start, pos - 1, StandardCharsets.UTF_8));
        if (arrSize == 0) {
            result.useSize = 4;
            result.value = new ArrayList<>();
            return result;
        }
        // *2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n
        List<Object> list = new ArrayList<>();
        int startPos = start + pos + 2;
        for (int i = 0; i < arrSize; i++) {
            ParseResult ele = tryToParse(startPos, data);
            if (ele == null) {
                return null;
            }
            startPos += ele.useSize;
            list.add(ele.value);
        }
        result.useSize = startPos - start;
        result.value = list;
        return result;
    }

    private static ParseResult parseString2(int start, byte[] data) {
        if (data.length < 3 + start) {
            return null;
        }
        int pos = findFirstCRLF(start, data);
        if (pos == -1) {
            return null;
        }
        // $6\r\nfoobar\r\n
        int len = Integer.parseInt(new String(data, 1 + start, pos - 1, StandardCharsets.UTF_8));
        if (data.length < pos + len + 2 + 2 + start) {
            return null;
        }
        ParseResult result = new ParseResult();
        result.useSize = pos + len + 2 + 2;
        result.value = new String(data, pos + 2 + start, len, StandardCharsets.UTF_8);
        return result;
    }

    private static ParseResult parseInteger(int start, byte[] data) {
        ParseResult result = parseString(start, data);
        if (result == null) {
            return null;
        }
        result.value = Integer.parseInt((String) result.value);
        return result;
    }

    private static ParseResult parseString(int start, byte[] data) {
        if (data.length < 3 + start) {
            return null;
        }
        int pos = findFirstCRLF(start, data);
        if (pos == -1) {
            return null;
        }
        // +OKKL\r\n
        ParseResult result = new ParseResult();
        result.useSize = pos + 2;
        result.value = new String(data, 1 + start, pos - 1, StandardCharsets.UTF_8);
        return result;
    }

    private static int findFirstCRLF(int start, byte[] data) {
        for (int i = 1 + start; i < data.length; i++) {
            byte b1 = data[i - 1];
            byte b2 = data[i];
            if (b1 == '\r' && b2 == '\n') {
                return i - 1 - start;
            }
        }
        return -1;
    }

    private static class ParseResult {
        private int useSize;
        private Object value;
    }

//    public static void main(String[] args) {
//        byte[] data = new byte[]{'-', '+', 'O', 'K', '\r', '\n'};
//        data = "1$6\r\nfoobar\r\n".getBytes(StandardCharsets.UTF_8);
//        data = "1*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n".getBytes(StandardCharsets.UTF_8);
//        ParseResult result = tryToParse(1, data);
//        if (result == null) {
//            System.out.println("null");
//        } else {
//            System.out.println("|" + result.value + "|");
//            System.out.println(result.useSize);
//        }
//    }
}
