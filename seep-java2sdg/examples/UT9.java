/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
import java.util.Vector;
import java.util.List;
import uk.ac.imperial.lsds.seep.api.Partitioned;
import uk.ac.imperial.lsds.seep.api.Partial;
import uk.ac.imperial.lsds.seep.api.DriverProgram;
import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;

public class UT9 implements DriverProgram{

	// Keep the money inserted by users
	@Partial
	public SeepMap<String, Integer> userid_money = new SeepMap<String, Integer>();
	// Keep the total money inserted by day
	@Partial
	public SeepMap<Integer, Integer> day_money = new SeepMap<Integer, Integer>();

	// The current day
	public int day = 2;

	public void main(){
		String userid = "user001";
		int money = 50;
		updateMoney(userid, money);
		int totalMoneyDay = totalMoneyDay(day);
		int totalMoneyUser = totalMoneyUser(userid);
	}

	public void updateMoney(String userid, int money){
		// First update total money per day
		@Global updateMoneyDay(day_money, day, money);
		// Then update total money for user
		int currentMoney = 0;
		if(userid_money.containsKey(userid)){
			currentMoney = (Integer)userid_money.get(userid);
		}
		int newMoney = currentMoney + money;
		userid_money.put(userid, newMoney);
	}

	private void updateMoneyDay(SeepMap<Integer, Integer> day_money, int day, int money){
		int moneyDay = 0;
		if(day_money.containsKey(day)){
			moneyDay = (Integer)day_money.get(day);
		}
		int newTotal = moneyDay + money;
		day_money.put(day, newTotal);
	}

	public int totalMoneyUser(String userid){
		@PartialData int total = countTotalPerUser(@Global userid_money, userid);
		int t = merge(total);
		return t;	
	}

	public int totalMoneyDay(int day){
		if(day_money.containsKey(day))
			return (Integer)day_money.get(day);
		else
			return 0;
	}

	private int countTotalPerUser(SeepMap<String, Integer> userid_money, String key){
		if(userid_money.containsKey(key))
			return (Integer)userid_money.get(key);
		else
			return 0;
	}

	private int merge(int counts){
		return 0;
	}

	private int merge(@Collection List<Integer> totalM){
		int m = 0;
		for(Integer money : totalM){
			m += money;
		}
		return m;
	}
}
