import java.util.Vector;
import uk.ac.imperial.lsds.seep.api.Partitioned;
import uk.ac.imperial.lsds.seep.api.DriverProgram;
import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;

public class UT6 implements DriverProgram{

	// Keep the money inserted by users
	@Partitioned
	public SeepMap<String, Integer> userid_money = new SeepMap<String, Integer>();
	// Keep the total money inserted by day
	@Partitioned
	public SeepMap<Integer, Integer> day_money = new SeepMap<Integer, Integer>();

	// The current day
	public int day = 2;

	public void main(){
		String userid = "user001";
		int money = 50;
		updateMoney(userid, money);
		int totalMoneyUser = totalMoneyUser(userid);
		int totalMoneyDay = totalMoneyDay(day);
	}

	public void updateMoney(String userid, int money){
		// First update total money for user
		int currentMoney = 0;
		if(userid_money.containsKey(userid)){
			currentMoney = (Integer)userid_money.get(userid);
		}
		int newMoney = currentMoney + money;
		userid_money.put(userid, newMoney);
		// Then update total money per day
		int moneyDay = 0;
		if(day_money.containsKey(day)){
			moneyDay = (Integer)day_money.get(day);
		}
		int newTotal = moneyDay + money;
		day_money.put(day, newTotal);
	}

	public int totalMoneyUser(String userid){
		int total = 0;
		if(userid_money.containsKey(userid)){
			total = (Integer)userid_money.get(userid);
		}
		return total;
	}

	public int totalMoneyDay(int day){
		int total = 0;
		if(day_money.containsKey(day)){
			total = (Integer)day_money.get(day);
		}
		return total;
	}
}
