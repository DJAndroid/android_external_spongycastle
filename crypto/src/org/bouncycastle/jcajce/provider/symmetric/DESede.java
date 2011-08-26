package org.bouncycastle.jcajce.provider.symmetric;

import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.DESedeWrapEngine;
import org.bouncycastle.crypto.engines.RFC3211WrapEngine;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher;

public final class DESede
{
    private DESede()
    {
    }

    static public class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new DESedeEngine());
        }
    }

    static public class CBC
        extends BaseBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new DESedeEngine()), 64);
        }
    }

    /**
     * DESede   CFB8
     */
    public static class DESedeCFB8
        extends BaseMac
    {
        public DESedeCFB8()
        {
            super(new CFBBlockCipherMac(new DESedeEngine()));
        }
    }

    /**
     * DESede64
     */
    public static class DESede64
        extends BaseMac
    {
        public DESede64()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64));
        }
    }

    /**
     * DESede64with7816-4Padding
     */
    public static class DESede64with7816d4
        extends BaseMac
    {
        public DESede64with7816d4()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64, new ISO7816d4Padding()));
        }
    }
    
    public static class CBCMAC
        extends BaseMac
    {
        public CBCMAC()
        {
            super(new CBCBlockCipherMac(new DESedeEngine()));
        }
    }

    static public class CMAC
        extends BaseMac
    {
        public CMAC()
        {
            super(new CMac(new DESedeEngine()));
        }
    }

    public static class Wrap
        extends BaseWrapCipher
    {
        public Wrap()
        {
            super(new DESedeWrapEngine());
        }
    }

    public static class RFC3211
        extends BaseWrapCipher
    {
        public RFC3211()
        {
            super(new RFC3211WrapEngine(new DESedeEngine()), 8);
        }
    }

  /**
     * DESede - the default for this is to generate a key in
     * a-b-a format that's 24 bytes long but has 16 bytes of
     * key material (the first 8 bytes is repeated as the last
     * 8 bytes). If you give it a size, you'll get just what you
     * asked for.
     */
    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        private boolean     keySizeSet = false;

        public KeyGenerator()
        {
            super("DESede", 192, new DESedeKeyGenerator());
        }

        protected void engineInit(
            int             keySize,
            SecureRandom random)
        {
            super.engineInit(keySize, random);
            keySizeSet = true;
        }

        protected SecretKey engineGenerateKey()
        {
            if (uninitialised)
            {
                engine.init(new KeyGenerationParameters(new SecureRandom(), defaultKeySize));
                uninitialised = false;
            }

            //
            // if no key size has been defined generate a 24 byte key in
            // the a-b-a format
            //
            if (!keySizeSet)
            {
                byte[]     k = engine.generateKey();

                System.arraycopy(k, 0, k, 16, 8);

                return new SecretKeySpec(k, algName);
            }
            else
            {
                return new SecretKeySpec(engine.generateKey(), algName);
            }
        }
    }

    /**
     * generate a desEDE key in the a-b-c format.
     */
    public static class KeyGenerator3
        extends BaseKeyGenerator
    {
        public KeyGenerator3()
        {
            super("DESede3", 192, new DESedeKeyGenerator());
        }
    }

    static public class KeyFactory
        extends BaseSecretKeyFactory
    {
        public KeyFactory()
        {
            super("DESede", null);
        }

        protected KeySpec engineGetKeySpec(
            SecretKey key,
            Class keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec == null)
            {
                throw new InvalidKeySpecException("keySpec parameter is null");
            }
            if (key == null)
            {
                throw new InvalidKeySpecException("key parameter is null");
            }

            if (SecretKeySpec.class.isAssignableFrom(keySpec))
            {
                return new SecretKeySpec(key.getEncoded(), algName);
            }
            else if (DESedeKeySpec.class.isAssignableFrom(keySpec))
            {
                byte[]  bytes = key.getEncoded();

                try
                {
                    if (bytes.length == 16)
                    {
                        byte[]  longKey = new byte[24];

                        System.arraycopy(bytes, 0, longKey, 0, 16);
                        System.arraycopy(bytes, 0, longKey, 16, 8);

                        return new DESedeKeySpec(longKey);
                    }
                    else
                    {
                        return new DESedeKeySpec(bytes);
                    }
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }

            throw new InvalidKeySpecException("Invalid KeySpec");
        }

        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof DESedeKeySpec)
            {
                DESedeKeySpec desKeySpec = (DESedeKeySpec)keySpec;
                return new SecretKeySpec(desKeySpec.getKey(), "DESede");
            }

            return super.engineGenerateSecret(keySpec);
        }
    }

    public static class Mappings
        extends HashMap
    {
        private static final String PREFIX = DESede.class.getName();
                
        public Mappings()
        {
            put("Cipher.DESEDE", PREFIX + "$ECB");
            put("Cipher." + PKCSObjectIdentifiers.des_EDE3_CBC, PREFIX + "$CBC");
            put("Cipher." + OIWObjectIdentifiers.desCBC, PREFIX + "$CBC");
            put("Cipher.DESEDEWRAP", PREFIX + "$Wrap");
            put("Cipher." + PKCSObjectIdentifiers.id_alg_CMS3DESwrap, PREFIX + "$Wrap");
            put("Cipher.DESEDERFC3211WRAP", PREFIX + "$RFC3211");

            put("KeyGenerator.DESEDE", PREFIX + "$KeyGenerator");
            put("KeyGenerator." + PKCSObjectIdentifiers.des_EDE3_CBC, PREFIX + "$KeyGenerator3");
            put("KeyGenerator.DESEDEWRAP", PREFIX + "$KeyGenerator");

            put("SecretKeyFactory.DESEDE", PREFIX + "$KeyFactory");

            put("Mac.DESEDECMAC", PREFIX + "$CMAC");
            put("Mac.DESEDEMAC", PREFIX + "$CBCMAC");
            put("Alg.Alias.Mac.DESEDE", "DESEDEMAC");

            put("Mac.DESEDEMAC/CFB8", PREFIX + "$DESedeCFB8");
            put("Alg.Alias.Mac.DESEDE/CFB8", "DESEDEMAC/CFB8");

            put("Mac.DESEDEMAC64", PREFIX + "$DESede64");
            put("Alg.Alias.Mac.DESEDE64", "DESEDEMAC64");

            put("Mac.DESEDEMAC64WITHISO7816-4PADDING", PREFIX + "$DESede64with7816d4");
            put("Alg.Alias.Mac.DESEDE64WITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
            put("Alg.Alias.Mac.DESEDEISO9797ALG1MACWITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
            put("Alg.Alias.Mac.DESEDEISO9797ALG1WITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
        }
    }
}