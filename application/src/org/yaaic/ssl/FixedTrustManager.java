/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari
Copyright 2013 Joshua Phillips

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.yaaic.ssl;

import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

/**
 * Trust manager that accepts only one fingerprint
 * 
 * @author Joshua Phillips <jphillips@imap.cc>
 */

 /* TODO TODO TODO */
public class FixedTrustManager implements X509TrustManager
{

    private byte[] _fingerprint = null;
    private static final String hexChars = "0123456789ABCDEF";

    public static byte[] parseDigest(String digest)
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int nextByte = 0;
        int nibbleIndex = 0;
        for (int i=0; i<digest.length(); ++i){
            char ch = Character.toUpperCase(digest.charAt(i));
            if (ch == ':'){
                /* skip */
            } else {
                int nibble = hexChars.indexOf(ch);
                if (nibble == -1){
                    return null;
                }
                nextByte = (nextByte << 4) | nibble;
                ++nibbleIndex;
                if (nibbleIndex == 2){
                    result.write(nextByte);
                    nextByte = 0;
                    nibbleIndex = 0;
                }
            }
        }
        if (nibbleIndex != 0){
            return null;
        }
        return result.toByteArray();
    }

    public FixedTrustManager(String fingerprint)
    {
        _fingerprint = parseDigest(fingerprint);
    }

    /**
     * Check client trusted
     * 
     * @throws CertificateException if not trusted
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        throw new CertificateException();
    }

    /**
     * Check server trusted
     * 
     * @throws CertificateException if not trusted
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        X509Certificate cert = chain[chain.length-1];
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e){
            throw new CertificateException("MessageDigest.getInstance threw NoSuchAlgorithmException");
        }
        md.update(cert.getEncoded());
        byte[] fp = md.digest();
        if (!Arrays.equals(fp, _fingerprint)){
            throw new CertificateException(
                "The server's certificate's fingerprint doesn't match the setting.");
        }
    }

    /**
     * Get accepted issuers
     */
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }
}
