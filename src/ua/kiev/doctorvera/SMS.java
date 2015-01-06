package ua.kiev.doctorvera;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Bodun on 19.08.2014.
 */
public class SMS {

    private Integer id;
    private String phoneNumber;
    private String text;
    private Byte state;
    private Date dateSent;
    private Date dateChanged;
    private Long trackingId;
    private static final HashMap <String, Byte> possibleStates = new HashMap <String, Byte>();

	public SMS() {
		setPossiblestates();
        state = 1;
    }
	
    public static void setPossiblestates() {
    	possibleStates.put("New",(byte)1);
    	possibleStates.put("Template",(byte)2);
    	possibleStates.put("Accepted",(byte)3);
    	possibleStates.put("Enroute",(byte)3);
    	possibleStates.put("Delivered",(byte)4);
    	possibleStates.put("Expired",(byte)5);
    	possibleStates.put("Deleted",(byte)5);
    	possibleStates.put("Undeliverable",(byte)6);
    	possibleStates.put("Rejected",(byte)6);
    	possibleStates.put("Unknown",(byte)5);
	}

    public static HashMap<String, Byte> getPossiblestates() {
		return possibleStates;
	}
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public Long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Long trackingId) {
        this.trackingId = trackingId;
    }

    public void setStateString(String state) {
         this.state = possibleStates.get(state);
    }

    public String getStateString() {
        switch (this.state) {
            case 2:
                return "Черновик";
            case 3:
                return "Отправлена";
            case 4:
                return "Доставлена";
            case 5:
                return "Ошибка доставки";
            case 6:
                return "Ошибка отправки";
            default:
                return "Новая";
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return phoneNumber + " " + text + " " + dateFormat.format(dateSent);
    }
}
