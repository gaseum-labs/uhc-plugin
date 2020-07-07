package com.codeland.uhc.command;

import java.io.*;
import java.util.*;

//THIS FILE WILL BE TURNED INTO KOTLIN EVENTUALLY, rn it will just serve as a reference
public class TeamMakerOld {
	private String dataPath;

	//private ArrayList<ScoredPlayer> players;

	public TeamMakerOld(String pathToData) {
		dataPath = pathToData;
		/*players = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(pathToData)));
			String line;
			while (br.ready()) {
				line = br.readLine();
				if (line.length() > 0) {
					ScoredPlayer n = new ScoredPlayer();
					n.username = line.substring(0, line.indexOf(" "));
					int start = line.indexOf(" ") + 1;
					n.score = Float.parseFloat(line.substring(start, line.indexOf(" ", start)));
					n.gameCount = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
					players.add(n);
				}
			}
			br.close();
		} catch (IOException ignored) { }*/
	}

	public static String[][] getTeamsRandom(ArrayList<String> names, int teamSize) {
		ArrayList<String[]> ret = new ArrayList<>();
		while (names.size() > 0) {
			String[] tem = new String[teamSize];
			for (int i = 0; i < tem.length; ++i) {
				if (names.size() > 0) {
					int rand = (int) (Math.random() * names.size());
					tem[i] = names.remove(rand);
				}
			}
			ret.add(tem);
		}
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("yo!");
			}
		}, 1000);

		return ret.toArray(new String[][] {});
	}

	/*public String[][] getTeamsRanked(String[] names, int teamSize) {
		ArrayList<ScoredPlayer> tempPlayers = new ArrayList<>();
		for (int i = 0; i < names.length; ++i) {
			tempPlayers.add(getPlayer(names[i]));
		}
		while (tempPlayers.size() % teamSize > 0) {
			tempPlayers.add(new ScoredPlayer(1.0f));
		}
		ArrayList<ScoredPlayer[]> spRet = new ArrayList<>();
		while (tempPlayers.size() > 0) {
			ScoredPlayer[] team = new ScoredPlayer[teamSize];
			for (int i = 0; i < teamSize; ++i) {
				team[i] = tempPlayers.get(0);
				tempPlayers.remove(0);
			}
			spRet.add(team);
		}
		while (doImproveRound(spRet));

		String[][] ret = new String[spRet.size()][teamSize];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = new String[teamSize];
			for (int j = 0; j < teamSize; ++j) {
				ret[i][j] = spRet.get(i)[j].username;
			}
		}
		return ret;
	}

	public String[][] getTeamsRandRanked(String[] names, int teamSize, float maxDisplace) {
		ArrayList<ScoredPlayer> tempPlayers = new ArrayList<>();
		for (int i = 0; i < names.length; ++i) {
			ScoredPlayer p = getPlayer(names[i]);
			p.score += maxDisplace * (Math.random() * 2 - 1);
			tempPlayers.add(getPlayer(names[i]));
		}
		while (tempPlayers.size() % teamSize > 0) {
			tempPlayers.add(new ScoredPlayer(1.0f));
		}
		ArrayList<ScoredPlayer[]> spRet = new ArrayList<>();
		while (tempPlayers.size() > 0) {
			ScoredPlayer[] team = new ScoredPlayer[teamSize];
			for (int i = 0; i < teamSize; ++i) {
				team[i] = tempPlayers.get(0);
				tempPlayers.remove(0);
			}
			spRet.add(team);
		}
		while (doImproveRound(spRet));

		String[][] ret = new String[spRet.size()][teamSize];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = new String[teamSize];
			for (int j = 0; j < teamSize; ++j) {
				ret[i][j] = spRet.get(i)[j].username;
			}
		}
		return ret;
	}

	private boolean doImproveRound(ArrayList<ScoredPlayer[]> teams) {
		boolean ret = false;
		for (int i = 0; i < teams.size(); ++i) {
			for (int j = i + 1; j < teams.size(); ++j) {
				if (trySwaps(teams.get(i), teams.get(j))) {
					ret = true;
				}
			}
		}
		return ret;
	}

	private boolean trySwaps(ScoredPlayer[] teama, ScoredPlayer[] teamb) {
		String tempName;
		float tempScore;
		int tempRounds;

		float teamAScore = calcScore(teama);
		float teamBScore = calcScore(teamb);
		float mean = teamAScore + teamBScore;
		mean /= 2;
		float beforeMSE = Math.abs(teamAScore - teamBScore);

		boolean ret = false;

		for (int i = 0; i < teama.length; ++i) {
			for (int j = 0; j < teamb.length; ++j) {
				float tempScoreA = teamAScore - teama[i].score + teamb[j].score;
				float tempScoreB = teamBScore - teamb[j].score + teama[i].score;
				float mse = Math.abs(tempScoreA - tempScoreB);
				if (mse < beforeMSE) {
					tempName = teama[i].username;
					tempScore = teama[i].score;
					tempRounds = teama[i].gameCount;
					teama[i].username = teamb[j].username;
					teama[i].score = teamb[j].score;
					teama[i].gameCount = teamb[j].gameCount;
					teamb[j].username = tempName;
					teamb[j].score = tempScore;
					teamb[j].gameCount = tempRounds;
					ret = true;
					beforeMSE = mse;
					teamAScore = tempScoreA;
					teamBScore = tempScoreB;
				}
			}
		}
		return ret;
	}

	private float calcScore(ScoredPlayer[] team) {
		float ret = 0;
		for (ScoredPlayer p : team) {
			ret += p.score;
		}
		return ret;
	}

	public void addGame(String[] loosers, String[] winners) {
		int totalPlayers = loosers.length + winners.length;
		for (int i = 0; i < loosers.length; ++i) {
			if (loosers[i] != null) {
				int idx = getIndex(loosers[i]);
				ScoredPlayer p = players.get(idx);
				float oldScoreWeight = p.score * p.gameCount;
				float newScore = (float) (i + winners.length) / (float) totalPlayers;
				p.gameCount += 1;
				p.score = (oldScoreWeight + newScore) / (float) p.gameCount;
			}
		}
		for (int i = 0; i < winners.length; ++i) {
			if (winners[i] != null) {
				int idx = getIndex(winners[i]);
				ScoredPlayer p = players.get(idx);
				float oldScoreWeight = p.score * p.gameCount;
				p.gameCount += 1;
				p.score = oldScoreWeight / (float) p.gameCount;
			}
		}
		saveData();
	}

	public void saveData() {
		try {
			FileWriter fr = new FileWriter(new File(dataPath), false);
			if (players.size() > 0) {
				fr.write(players.get(0).toString());
				for (int i = 1; i < players.size(); ++i) {
					if (players.get(i).username != null) {
						fr.write("\n" + players.get(i).toString());
					}
				}
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ScoredPlayer> getSorted() {
		ArrayList<ScoredPlayer> sorted = (ArrayList<ScoredPlayer>) players.clone();
		sorted.sort((ScoredPlayer o1, ScoredPlayer o2) -> {
			if (o1.score > o2.score) {
				return 1;
			}
			return -1;
		});
		return sorted;
	}

	private float getScore(String username) {
		for (ScoredPlayer p : players) {
			if (p.username.equals(username)) {
				return p.score;
			}
		}
		return 0.5f;
	}

	private int getIndex(String name) {
		for (int i = 0; i < players.size(); ++i) {
			if (players.get(i).username != null) {
				if (players.get(i).username.equals(name)) {
					return i;
				}
			}
		}
		ScoredPlayer p = new ScoredPlayer();
		p.username = name;
		p.score = 0.5f;
		p.gameCount = 0;
		players.add(p);
		return players.size() - 1;
	}

	private ScoredPlayer getPlayer(String username) {
		return players.get(getIndex(username));
	}*/
}
/*
8

MCRewind 0.875
IkkakuChan 0.75
Cutarez 0.625
Shiverisbjorn 0.5
ThrftstorGestapo 0.375
Jungle_Master 0.25
tied win
Varas_ balduvian 0
*/

/*
10

Jungle_Master 0.9
Deinocheirus 0.8
Dancegirl05 0.7
MCRewind 0.6
Cutarez 0.5
IkkakuChan 0.4
shiverisbjorn 0.3
ThrftstorGestapo 0.2
tied win
Varas_ balduvian 0

*/

/*

IkkakuChan
dancegirl05
Deinocheirus
BanjoNKazooie
balduvian
tazwell
ZibboTheGreat
MCRewind
uglydollninja
Jungle_Master
tied win
Varas_ shiverisbjorn

 */

/*
MCRewind
IkakkuChan
balduvian
ZibboTheGreat
dancegirl05
uglydollninja
Varas_
tied win
Jungle_Master shiverisbjorn
 */