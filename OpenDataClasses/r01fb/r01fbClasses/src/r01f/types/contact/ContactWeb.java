package r01f.types.contact;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.weburl.SerializedURL;


/**
 * Contact's web sites
 * <pre class='brush:java'>
 *	ContactWeb user = ContactWeb.createToBeUsedFor(ContactInfoUsage.PERSONAL)
 *								.url(WebUrl.of("www.futuretelematics.net"));
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="webChannel")
@Accessors(prefix="_")
@NoArgsConstructor
public class ContactWeb 
     extends ContactInfoMediaBase<ContactWeb> {
	
	private static final long serialVersionUID = -4012809208590547328L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Web
	 */
	@XmlElement(name="url")
	@Getter @Setter private SerializedURL _web;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static ContactWeb createToBeUsedFor(final ContactInfoUsage usage) {
		ContactWeb outNetwork = new ContactWeb();
		outNetwork.usedFor(usage);
		return outNetwork;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public ContactWeb url(final SerializedURL web) {
		_web = web;
		return this;
	}
	public ContactWeb url(final String web) {
		_web = SerializedURL.create(web);
		return this;
	}
}
