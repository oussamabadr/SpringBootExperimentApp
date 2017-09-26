/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.badr.ordermanagement.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;

/**
 *
 * @author oussama
 */
@Entity
@NoArgsConstructor
@Getter @Setter
public class Customer extends AbstractBaseEntity{

	@Column
	@Email
	private String email;
	
    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Embedded
    private Address address;

	@Setter(AccessLevel.NONE)
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "BONUSCARD_ID")
    private BonusCard bonusCard = null;

	@Setter(AccessLevel.NONE)
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "CREDITCARD_ID")
    private List<CreditCard> creditCards = new ArrayList<>();

	
	public void setBonusCard(BonusCard bonusCard) {
		if (this.bonusCard != null){
			throw new UnsupportedOperationException("Le consommateur a déjà une carte de fidélité!");
		}
		this.bonusCard = bonusCard;
	}

	public Boolean addCreditCard(CreditCard creditCards) {
		if ( !(creditCards.isValid()) ){
			return false;
		}
		return this.creditCards.add(creditCards);
	}

}
