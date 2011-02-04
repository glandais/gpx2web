import java.applet.Applet;

public class MagicApplet extends Applet {

	private static final long serialVersionUID = -2195226528049983022L;

	public MagicApplet() {
		super();
		createPopup();
	}

	private void createPopup() {
		MagicPopup magicPopup = new MagicPopup(this);

		magicPopup.setAlwaysOnTop(true);

		magicPopup.setLocationByPlatform(true);
		magicPopup.pack();
		magicPopup.setVisible(true);
	}

}
