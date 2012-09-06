package es.prodevelop.android.common.encrypt;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import es.prodevelop.gvsig.mini.common.IEncryptor;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Usage:
 * 
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 * 
 * @author ferenc.hechler
 */
public class Encryption extends IEncryptor {

	Context context;

	public Encryption(Context context) {
		this.context = context;
	}

	@Override
	public String getUniqueID() {
		return Settings.Secure.ANDROID_ID;
	}

	@Override
	public String getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) context
		.getSystemService(Context.TELEPHONY_SERVICE);
return telephonyManager.getDeviceId();
	}

}
