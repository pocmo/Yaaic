/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

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
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Naive Trust Manager that accepts every certificate
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class NaiveTrustManager implements X509TrustManager
{
    /**
     * Check client trusted
     * 
     * @throws CertificateException if not trusted
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        // No Exception == Trust
    }

    /**
     * Check server trusted
     * 
     * @throws CertificateException if not trusted
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        // No Exception == Trust
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
