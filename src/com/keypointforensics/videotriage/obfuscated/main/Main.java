package com.keypointforensics.videotriage.obfuscated.main;

import com.keypointforensics.videotriage.environment.Environment;
import com.keypointforensics.videotriage.gui.main.SelectCaseDialog;
import com.keypointforensics.videotriage.gui.splash.SplashScreen;
import com.keypointforensics.videotriage.terms.TermsVerifierWindow;
import com.keypointforensics.videotriage.thread.TermsVerificationThread;
public class Main {

	public Main() {
		
	}
	
	public void run() {
		Environment.setup();
		
		final TermsVerifierWindow termsVerifierWindow = new TermsVerifierWindow();

		if(termsVerifierWindow.needsVerification() == true) {
			termsVerifierWindow.buildAndDisplay();
			TermsVerificationThread termsVerificationThread = termsVerifierWindow.getVerificationThread();
		
			try {
				termsVerificationThread.join();
			} catch (InterruptedException e) {
				System.err.println("SEVERE error. Error ID: 003099. Quitting...");
				
				System.exit(0);
			}
			
			if(termsVerificationThread.verificationSuccessful() == false) {
				//
				
				System.exit(0);
			} else {
				termsVerificationThread.verificationAttempted(true);
				termsVerificationThread.interrupt();
				termsVerificationThread.stop();
			}
		}
		
		SplashScreen splashScreen = new SplashScreen();
		splashScreen.display();

		SelectCaseDialog selectCaseDialog = new SelectCaseDialog();

		splashScreen.dispose();
	}
}
