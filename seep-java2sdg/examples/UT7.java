import java.util.Vector;
import java.util.List;
import uk.ac.imperial.lsds.seep.api.Partitioned;
import uk.ac.imperial.lsds.seep.api.Partial;
import uk.ac.imperial.lsds.seep.api.DriverProgram;
import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;

public class UT7 implements DriverProgram{

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
		@PartialData int totalUser = countTotalPerUser(@Global userid_money, userid);
		int tUser = merge(totalUser);
		return tUser;
	}

	public int totalMoneyDay(int day){
		@PartialData int totalDay = countTotalMoney(@Global day_money, day);
		int tDay = merge(totalDay);
		return tDay;
	}

	private int countTotalPerUser(SeepMap<String, Integer> userid_money, String key){
		if(userid_money.containsKey(key))
			return (Integer)userid_money.get(key);
		else
			return 0;
	}

	private int countTotalMoney(SeepMap<Integer, Integer> day_money, int key){
		if(day_money.containsKey(key))
			return (Integer)day_money.get(key);
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
