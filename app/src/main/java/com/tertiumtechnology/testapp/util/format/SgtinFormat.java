package com.tertiumtechnology.testapp.util.format;

public class SgtinFormat {
    private static String byteToHex(int val) {
        byte tmp = (byte)(val % 256);
        return String.format("%02X", tmp);
    }

    public static byte[] SGTIN96toEPC(String sgtin96) throws BadFormatException {
        byte[] EPC = new byte[12];
        int digits;

        EPC[0] = 0x30;
        int dot1 = sgtin96.indexOf('.');
        if (dot1 <= 0)
            throw new BadFormatException();
        String filter = sgtin96.substring(0, dot1);
        if (filter.length() !=  1)
            throw new BadFormatException();
        EPC[1] = (byte)((Byte.parseByte(filter) << 5) & 0xE0); // 3 bit
        int dot2 = sgtin96.indexOf('.', dot1+1);
        if (dot2 <= 0)
            throw new BadFormatException();
        String company = sgtin96.substring(dot1+1, dot2);
        digits = company.length();
        if (digits < 6 || digits > 12)
            throw new BadFormatException();
        byte partition = (byte)(12 - digits);
        EPC[1] |= (partition << 2) & 0x1C; // 3 bit
        int dot3 = sgtin96.indexOf('.', dot2+1);
        if (dot3 <= 0)
            throw new BadFormatException();
        String item = sgtin96.substring(dot2+1, dot3);
        digits = item.length();
        //if (digits != partition+1)
        if (digits > partition+1)
            throw new BadFormatException();
        long company_bin = Long.parseLong(company);
        int item_bin = Integer.parseInt(item);
        switch (partition) {
            case 0: // 40 bits for company + 4 bits for item
                EPC[1] |= (byte)((company_bin >>> 38) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 30) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 22) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin >>> 14) & 0xFF); // 8 bit
                EPC[5] =  (byte)((company_bin >>> 6) & 0xFF); // 8 bit
                EPC[6] =  (byte)((company_bin << 2) & 0xFC); // 6 bit
                EPC[6] |= (byte)((item_bin >>> 2) & 0x03); // 2 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 1: // 37 bits for company + 7 bits for item
                EPC[1] |= (byte)((company_bin >>> 35) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 27) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 19) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin >>> 11) & 0xFF); // 8 bit
                EPC[5] =  (byte)((company_bin >>> 6) & 0xFF); // 8 bit
                EPC[6] =  (byte)((company_bin << 5) & 0xE0); // 3 bit
                EPC[6] |= (byte)((item_bin >>> 2) & 0x1F); // 5 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 2: // 34 bits for company + 10 bits for item
                EPC[1] |= (byte)((company_bin >>> 32) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 24) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 16) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin >>> 8) & 0xFF); // 8 bit
                EPC[5] =  (byte)(company_bin & 0xFF); // 8 bit
                EPC[6] =  (byte)((item_bin >>> 2) & 0xFF); // 8 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 3: // 30 bits for company + 14 bits for item
                EPC[1] |= (byte)((company_bin >>> 28) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 20) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 12) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin >>> 4) & 0xFF); // 8 bit
                EPC[5] =  (byte)((company_bin << 4) & 0xF0); // 4 bit
                EPC[5] |= (byte)((item_bin >>> 10) & 0x0F); // 4 bit
                EPC[6] =  (byte)((item_bin >>> 2) & 0xFF); // 8 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 4: // 27 bits for company + 17 bits for item
                EPC[1] |= (byte)((company_bin >>> 25) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 17) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 9) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin >>> 1) & 0xFF); // 8 bit
                EPC[5] =  (byte)((company_bin << 7) & 0x80); // 1 bit
                EPC[5] |= (byte)((item_bin >>> 10) & 0x7F); // 7 bit
                EPC[6] =  (byte)((item_bin >>> 2) & 0xFF); // 8 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 5: // 24 bits for company + 20 bits for item
                EPC[1] |= (byte)((company_bin >>> 22) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 14) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 6) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin << 2) & 0xFC); // 6 bit
                EPC[4] |= (byte)((item_bin >>> 18) & 0x03); // 2 bit
                EPC[5] =  (byte)((company_bin << 7) & 0x80); // 8 bit
                EPC[6] =  (byte)((item_bin >>> 2) & 0xFF); // 8 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
            case 6: // 20 bits for company + 24 bits for item
                EPC[1] |= (byte)((company_bin >>> 18) & 0x03); // 2 bit
                EPC[2] =  (byte)((company_bin >>> 10) & 0xFF); // 8 bit
                EPC[3] =  (byte)((company_bin >>> 2) & 0xFF); // 8 bit
                EPC[4] =  (byte)((company_bin << 6) & 0xC0); // 2 bit
                EPC[4] |= (byte)((item_bin >>> 18) & 0x3F); // 6 bit
                EPC[5] =  (byte)((item_bin >>> 10) & 0xFF); // 8 bit
                EPC[6] =  (byte)((item_bin >>> 2) & 0xFF); // 8 bit
                EPC[7] =  (byte)((item_bin << 6) & 0xC0); // 2 bit
                break;
        }
        String serial = sgtin96.substring(dot3+1, sgtin96.length());
        digits = serial.length();
        if (digits > 12)
            throw new BadFormatException();
        long serial_bin = Long.parseLong(serial);
        EPC[7] |= (byte)((serial_bin >>> 32) & 0x3F); // 6 bit
        EPC[8] =  (byte)((serial_bin >>> 24) & 0xFF); // 8 bit
        EPC[9] =  (byte)((serial_bin >>> 16) & 0xFF); // 8 bit
        EPC[10] = (byte)((serial_bin >>> 8) & 0xFF); // 8 bit
        EPC[11] = (byte)(serial_bin & 0xFF); // 8 bit
        return EPC;
    }

    public static String SGTIN96toEPChex(String sgtin96) throws BadFormatException {
        byte[] epc = SGTIN96toEPC(sgtin96);
        String hex = "";

        for (int n=0; n<epc.length; n++) {
            String tmp = byteToHex(epc[n]);
            hex += tmp;
        }
        return hex;
    }
}

