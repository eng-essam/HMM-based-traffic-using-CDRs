/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.Date;

/**
 *
 * @author essam
 */
public class CDR {

	public int id;
	public Date date;
	public int twr_id;

	public Date getDate() {
		return date;
	}

	public int getId() {
		return id;
	}

	public int getTwr_id() {
		return twr_id;
	}

	public void set_twr_id(int twr_id) {
		this.twr_id = twr_id;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setId(int id) {
		this.id = id;
	}

}
