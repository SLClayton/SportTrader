
package com.globalbettingexchange.externalapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PolarityEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PolarityEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="For"/&gt;
 *     &lt;enumeration value="Against"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "PolarityEnum")
@XmlEnum
public enum PolarityEnum {

    @XmlEnumValue("For")
    FOR("For"),
    @XmlEnumValue("Against")
    AGAINST("Against");
    private final String value;

    PolarityEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PolarityEnum fromValue(String v) {
        for (PolarityEnum c: PolarityEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
