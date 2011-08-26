package org.bouncycastle.asn1.cms;

import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;

public class IssuerAndSerialNumber
    extends ASN1Object
{
    private X500Name    name;
    private DERInteger  serialNumber;



    public static IssuerAndSerialNumber getInstance(
        Object  obj)
    {
        if (obj instanceof IssuerAndSerialNumber)
        {
            return (IssuerAndSerialNumber)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new IssuerAndSerialNumber((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException(
            "Illegal object in IssuerAndSerialNumber: " + obj.getClass().getName());
    }

    public IssuerAndSerialNumber(
        ASN1Sequence    seq)
    {
        this.name = X500Name.getInstance(seq.getObjectAt(0));
        this.serialNumber = (DERInteger)seq.getObjectAt(1);
    }

    public IssuerAndSerialNumber(
        X509CertificateStructure certificate)
    {
        this.name = certificate.getIssuer();
        this.serialNumber = certificate.getSerialNumber();
    }

    public IssuerAndSerialNumber(
        X500Name name,
        BigInteger  serialNumber)
    {
        this.name = name;
        this.serialNumber = new DERInteger(serialNumber);
    }

    /**
     * @deprecated use X500Name constructor
     */
    public IssuerAndSerialNumber(
        X509Name    name,
        BigInteger  serialNumber)
    {
        this.name = X500Name.getInstance(name);
        this.serialNumber = new DERInteger(serialNumber);
    }

    /**
     * @deprecated use X500Name constructor
     */
    public IssuerAndSerialNumber(
        X509Name    name,
        DERInteger  serialNumber)
    {
        this.name = X500Name.getInstance(name);
        this.serialNumber = serialNumber;
    }

    public X500Name getName()
    {
        return name;
    }

    public DERInteger getSerialNumber()
    {
        return serialNumber;
    }

    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();

        v.add(name);
        v.add(serialNumber);

        return new DERSequence(v);
    }
}