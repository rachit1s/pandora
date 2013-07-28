package meetingScheduling;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/*
Interview Street (Amazon Coding Challenge) : Meeting Schedules

Given M busy-time slots of N people, You need to print all the available time slots when all the N people can schedule a meeting for a duration of K minutes.
Event time will be of form HH MM ( where 0 <= HH <= 23 and 0 <= MM <= 59 ), K will be in the form minutes.
Input Format:
M K [ M number of busy time slots , K is the duration in minutes ]
Followed by M lines with 4 numbers on each line.
Each line will be of form StartHH StartMM EndHH EndMM  [ Example 9Am-11Am time slot will be given as 9 00 11 00 ]
An event time slot is of form [Start Time, End Time ) . Which means it inclusive at start time but doesn’t include the end time. 
So an event of form 10 00  11 00 => implies that the meeting start at 10:00 and ends at 11:00, so another meeting can start at 11:00.
Sample Input:
5 120
16 00 17 00
10 30 14 30
20 45 22 15
10 00 13 15
09 00 11 00
Sample Output:
00 00 09 00
17 00 20 45
 Sample Input:
8 60
08 00 10 15
22 00 23 15
17 00 19 00
07 00 09 45
09 00 13 00
16 00 17 45
12 00 13 30
11 30 12 30
Sample Output:
00 00 07 00
13 30 16 00
19 00 22 00
Constraints :
1 <= M <= 100
Note: 24 00 has to be presented as 00 00.
 */
public class Solution {
	
	static class MeetingTimes
	{
		int startHH;
		int startMM;
		int endHH;
		int endMM;
		
		MeetingTimes(int startMin, int endMin)
		{
			startHH = startMin/60;
			startMM = startMin % 60;
			endHH = endMin / 60;
			endMM = endMin % 60;
		}
		
		MeetingTimes()
		{
			
		}
		
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(getPadding(startHH)).append(" ").append(getPadding(startMM)).append(" ").append(getPadding(endHH)).append(" ").append(getPadding(endMM));
			return sb.toString();
		}

		private String getPadding(int i) {
			if( i < 10)
				return "0" + i;
			else return "" + i;
		}
	}
	
	static class MT
	{
		int startMin;
		int endMin;
		
		public MT(MeetingTimes mt)
		{
			startMin = (60*mt.startHH) + mt.startMM;
			endMin = (60*mt.endHH) + mt.endMM;
		}
		
		public String toString()
		{
			return startMin + ":" + endMin ;
		}
	}
	
	public static List<MeetingTimes> scheduleMeeting(List<MeetingTimes> meetings, int k)
	{
//		System.out.println("Meetings : " + meetings );
		List<MeetingTimes> slots = new ArrayList<MeetingTimes>();
		
		List<MT> mts = new ArrayList<MT>(meetings.size());
		for( MeetingTimes mt : meetings )
		{
			mts.add(new MT(mt));
		}
		
		Collections.sort(mts, new Comparator<MT>() {
			@Override
			public int compare(MT o1, MT o2) {
				if( o1.startMin < o2.startMin )
					return -1;
				else if ( o1.startMin > o2.startMin )
					return 1;
				return 0;
			}
		});
		
		List<MeetingTimes> tempSorted = new ArrayList<Solution.MeetingTimes>(mts.size());
		for( MT mt : mts )
		{
			tempSorted.add(new MeetingTimes(mt.startMin,mt.endMin));
		}
		
//		System.out.println("Sorted MeetingTimes : " + tempSorted);
//		System.out.println("sorted mts : " + mts );
		
		if( mts.size() >= 1 ) 
		{
			if( mts.get(0).startMin > k )
			{
				slots.add(new MeetingTimes(0, mts.get(0).startMin));
			}
		}
		
		int emax = mts.get(0).endMin;
		
		for( int i = 1 ; i < mts.size() ; i++ )
		{
			// see if the adjucent
			MT nextMeeting = mts.get(i);
			int diff = nextMeeting.startMin - emax;
			if( diff > 0 && diff >= k )
			{
				slots.add(new MeetingTimes(emax,nextMeeting.startMin));
			}
			
			if( emax < nextMeeting.endMin )
				emax = nextMeeting.endMin;
		}
		
		// find the last time
		int eod = (60*23 + 59);
		int lastDiff = emax - eod ;
		if( lastDiff > 0 && lastDiff >= k )
			slots.add(new MeetingTimes(emax,eod));
		
		return slots;
	}
	
	public static void main(String[] args) {
		int M, K;
		Scanner scan = new Scanner(System.in);
		M = scan.nextInt();
		K = scan.nextInt();
		scan.nextLine();
		
		List<MeetingTimes> mts = new ArrayList<MeetingTimes>(M);
		for( int i = 0 ; i < M ; i++ )
		{
			MeetingTimes mt = new MeetingTimes();
			mt.startHH = scan.nextInt();
			mt.startMM = scan.nextInt();
			mt.endHH = scan.nextInt();
			mt.endMM = scan.nextInt();
			mts.add(mt);
			scan.nextLine();
		}
		
		List<MeetingTimes> slots = scheduleMeeting(mts, K);
		
		for( MeetingTimes mt : slots )
		{
			System.out.println(mt);
		}
	}
}
