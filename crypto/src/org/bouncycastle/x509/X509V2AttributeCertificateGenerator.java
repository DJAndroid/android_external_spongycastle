package org.bouncycastle.x509;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttCertIssuer;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import org.bouncycastle.asn1.x509.V2AttributeCertificateInfoGenerator;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * class to produce an X.509 Version 2 AttributeCertificate.
 */
public class X509V2AttributeCertificateGenerator
{
    private V2AttributeCertificateInfoGenerator   acInfoGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = new Hashtable();
    private Vector                      extOrdering = new Vector();

    public X509V2AttributeCertificateGenerator()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extensions.clear();
        extOrdering.clear();
    }

    /**
     * Set the Holder of this Attribute Certificate
     */
    public void setHolder(
        AttributeCertificateHolder     holder)
    {
        acInfoGen.setHolder(holder.holder);
    }

    /**
     * Set the issuer
     */
    public void setIssuer(
        AttributeCertificateIssuer  issuer)
    {
        acInfoGen.setIssuer(AttCertIssuer.getInstance(issuer.form));
    }

    /**
     * set the serial number for the certificate.
     */
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        acInfoGen.setSerialNumber(new DERInteger(serialNumber));
    }

    public void setNotBefore(
        Date    date)
    {
        acInfoGen.setStartDate(new DERGeneralizedTime(date));
    }

    public void setNotAfter(
        Date    date)
    {
        acInfoGen.setEndDate(new DERGeneralizedTime(date));
    }

    /**
     * Set the signature algorithm. This can be either a name or an OID, names
     * are treated as case insensitive.
     * 
     * @param signatureAlgorithm string representation of the algorithm name.
     */
    public void setSignatureAlgorithm(
        String  signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;

        try
        {
            sigOID = X509Util.getAlgorithmOID(signatureAlgorithm);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unknown signature type requested");
        }

        sigAlgId = new AlgorithmIdentifier(this.sigOID, new DERNull());

        acInfoGen.setSignature(sigAlgId);
    }
    
    /**
     * add an attribute
     */
    public void addAttribute(
        X509Attribute       attribute)
    {
        acInfoGen.addAttribute(Attribute.getInstance(attribute.toASN1Object()));
    }

    public void setIssuerUniqueId(
        boolean[] iui)
    {
        // [TODO] convert boolean array to bit string
        //acInfoGen.setIssuerUniqueID(iui);
        throw new RuntimeException("not implemented (yet)");
    }
     
    /**
     * add a given extension field for the standard extensions tag
     * @throws IOException
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        ASN1Encodable   value)
        throws IOException
    {
        this.addExtension(OID, critical, value.getEncoded());
    }

    /**
     * add a given extension field for the standard extensions tag
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        DERObjectIdentifier oid = new DERObjectIdentifier(OID);
        
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     * @deprecated use generate()
     */
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateCertificate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     * @deprecated use generate()
     */
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generate(key, provider, random);
        }
        catch (NoSuchProviderException e)
        {
            throw e;
        }
        catch (SignatureException e)
        {
            throw e;
        }
        catch (InvalidKeyException e)
        {
            throw e;
        }
        catch (GeneralSecurityException e)
        {
            throw new SecurityException("exception creating certificate: " + e);
        }
    }

   /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509AttributeCertificate generate(
        PrivateKey      key,
        String          provider)
       throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, SignatureException, InvalidKeyException, NoSuchAlgorithmException
   {
        return generate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     */
    public X509AttributeCertificate generate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        if (!extensions.isEmpty())
        {
            acInfoGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }

        AttributeCertificateInfo acInfo = acInfoGen.generateAttributeCertificateInfo();

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(acInfo);
        v.add(sigAlgId);

        try
        {
            v.add(new DERBitString(X509Util.getSignatureForObject(sigOID, signatureAlgorithm, provider, key, random, acInfo)));

            return new X509V2AttributeCertificate(new AttributeCertificate(new DERSequence(v)));
        }
        catch (IOException e)
        {
            throw new ExtCertificateEncodingException("constructed invalid certificate", e);
        }
    }

    /**
     * Return an iterator of the signature names supported by the generator.
     * 
     * @return an iterator containing recognised names.
     */
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}
