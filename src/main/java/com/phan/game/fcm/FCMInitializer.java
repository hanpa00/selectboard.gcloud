package com.phan.game.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;



@Service
public class FCMInitializer {

	@Value("${app.firebase-configuration-file:google/fir-demo-84240-71246cd63f1e.json}")
	private String firebaseConfigPath;
	private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
	private static final String[] SCOPES = { MESSAGING_SCOPE };

	//Logger logger = LoggerFactory.getLogger(FCMInitializer.class);

	
	@PostConstruct
	public void initialize() {
		System.out.println("Firebase config file: " + firebaseConfigPath);    	
		try {
			//            FirebaseOptions options = new FirebaseOptions.builder()
			//                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())).build();
			//            if (FirebaseApp.getApps().isEmpty()) {
			//                FirebaseApp.initializeApp(options);
			//                System.out.println("Firebase application has been initialized");
			//            }
			//FileInputStream refreshToken = new FileInputStream(firebaseConfigPath);
			//            InputStream refreshToken = new ClassPathResource(firebaseConfigPath).getInputStream();                        
			//            FirebaseOptions options = FirebaseOptions.builder()
			//                .setCredentials(GoogleCredentials.fromStream(refreshToken))
			//                .build();


			FileInputStream serviceAccount = new FileInputStream("./src/main/resources/google/fir-demo-84240-71246cd63f1e.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();
			System.out.println("Firebase config file: " + serviceAccount.toString());
			System.out.println("Firebase initialize with options: " + options.toString());
			FirebaseApp.initializeApp(options);

			System.out.println("Firebase App: " + FirebaseApp.getApps().toString());
			//System.out.println("Tokens: " + getInputstreamString(refreshToken));
			System.out.println("Firebase Messaging: " + FirebaseMessaging.getInstance().toString());

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private String getInputstreamString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		byte[] byteArray = buffer.toByteArray();

		String text = new String(byteArray, StandardCharsets.UTF_8);
		return text;
	}

	
}