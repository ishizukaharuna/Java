package jp.alhinc.ishizuka_haruna.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
class Branch {

	public static void main(String[]args){
		HashMap<String,String>branchmap = new HashMap<String,String>();
		HashMap<String,String>commodiymap = new HashMap<String,String>();
		HashMap<String,Long> brEarmap = new HashMap<String,Long>();
		HashMap<String,Long> coEarmap = new HashMap<String,Long>();

		ArrayList<File> rcdfiles = new ArrayList<File>();
		ArrayList<Long> numberlist = new ArrayList<Long>();

		//【branch.lst（支店定義ファイル）の読み込み】
		try{
			File file = new File(args[0],"branch.lst");
			if(!file.exists()){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader (fr);
			String b;
			while((b = br.readLine()) !=null){
				//一行ずつ","で分割する
				String[]branch = b.split(",");
				if(branch.length == 2 && branch[0].matches("^\\d{3}$")){
				}else{
					System.out.println("支店定義ファイルのフォーマットが不正です");
					break;
				}
				//支店コードマップ
				branchmap.put(branch[0],branch[1]);
				//支店の売上マップ
				brEarmap.put(branch[0], 0L);
			}
			br.close();
		}catch(IOException e){
			System.out.println(e);
		}

		//【commodiy.lst（商品定義ファイル）の読み込み】
		try{
			File file = new File(args[0],"commodity.lst");
			if(!file.exists()){
				System.out.println("商品定義ファイルが存在しません。");
				return;
			}
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader (fr);
			String c;
			while((c=br.readLine()) !=null){
				//一行ずつ","で分割する
				String[] commodiy =c.split(",");
				if(commodiy.length == 2 && commodiy[0].matches("^[0-9a-zA-Z]{8}$")){
//					System.out.println(commodiy[0]+commodiy[1]);
				}else{
					System.out.println("商品定義ファイルのフォーマットが不正です。");
					break;
				}
				//商品名マップ
				commodiymap.put(commodiy[0],commodiy[1]);
				//商品の売上マップ
				coEarmap.put(commodiy[0], 0L);
			}
			br.close();
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
		}
		//inputディレクトリ読み込み→rcdファイルをリスト化
		File dir = new File(args[0]);
		File[] inputdir = dir.listFiles();
		for(int i =0; i<inputdir.length;i++){
			if(inputdir[i].getName().matches("^\\d{8}.rcd$") ){
				rcdfiles.add(inputdir[i]);
			}
		}
		int ii = 0;
		for(int i = 0 ; i<rcdfiles.size(); i++){
			File s = rcdfiles.get(i);
			String d = s.getName();
			String[] rcd =d.split("\\.");
			Long number = Long.parseLong(rcd[0]);
			numberlist.add(number);
			ii = i-1;
			if(i>=1){
				if(numberlist.get(i)- numberlist.get(ii) == 1){
				}else{
					System.out.println(numberlist.get(i)+"の売上ファイル名が連番になっていません");
					return;
				}
			}else{
			}
		}

		//【rcdのファイルの読み込み（支店・商品・売上）】
		for(int i = 0; i<rcdfiles.size(); i++){
			try{
				ArrayList<String> rcdlists = new ArrayList<String>();
				File rcd = rcdfiles.get(i);
				FileReader fr = new FileReader(rcd);
				BufferedReader br =new BufferedReader(fr);
				String s;
				while((s=br.readLine()) !=null){
					//rcdの中身をリストに追加する
					rcdlists.add(s);
//					System.out.println(rcdlists);
				}

				File r = rcdfiles.get(i);
				String d = r.getName();
				if(branchmap.get(rcdlists.get(0)) == null){
					System.out.print(d+"の支店コードは不正です");
					break;
				}else{
				}
				if(commodiymap.get(rcdlists.get(1)) == null){
					System.out.println(d+"の商品コードは不正です");
					break;
				}else{
				}
				if(rcdlists.size() != 3 ){
					System.out.println(d+"のフェイルフォーマットが不正です");
					break;
				}

				//支店の売上をマップに置く（集計）
				Long brEar = Long.parseLong(rcdlists.get(2));
				Long brTotal = brEarmap.get(rcdlists.get(0)) + brEar;
				brEarmap.put(rcdlists.get(0), brTotal);
				String brvaluse = Long.toString(brEarmap.get(rcdlists.get(0)));
				if(brvaluse.matches("^\\d{1,10}$")){
				}else{
					System.out.println("合計金額が10桁を超えました");
					break;
				}
				//商品別の売上をマップに置く（集計）
				Long coTotal = coEarmap.get(rcdlists.get(1)) + brEar;
				coEarmap.put(rcdlists.get(1), coTotal);
				br.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました。");
			}
		}

		//【ファイル書き込み】
		//brTotal（支店別売上集計map）降順の指定
		List<Map.Entry<String,Long>> brentries =
			new ArrayList<Map.Entry<String,Long>>(brEarmap.entrySet());
		Collections.sort(brentries, new Comparator<Map.Entry<String,Long>>() {

			@Override
	   		public int compare(
	   		Entry<String,Long> entry1, Entry<String,Long> entry2) {
	    	return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
	    	}
		});

//		branch.outに書き込み
		try{
			File file = new File(args[0],"branch.out");
			FileWriter fw = new FileWriter(file);
			BufferedWriter brOut = new BufferedWriter(fw);
				for(Entry<String,Long> s :brentries){
					brOut.write(s.getKey() + "," + branchmap.get(s.getKey()) + "," + s.getValue()+"\r\n");
				}
			brOut.close();
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
		}


		//coTotal（商品別売上集計map）降順の指定
		List<Map.Entry<String,Long>> coentries =
			new ArrayList<Map.Entry<String,Long>>(coEarmap.entrySet());
		Collections.sort(coentries, new Comparator<Map.Entry<String,Long>>() {

		@Override
			public int compare(
			Entry<String,Long> entry1, Entry<String,Long> entry2) {
			return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		//commodiy.outに書き込み
		try{
			File file = new File(args[0],"commodiy.out");
			FileWriter fw = new FileWriter(file);
			BufferedWriter coOut = new BufferedWriter(fw);
				for(Entry<String,Long> s :coentries){
					coOut.write(s.getKey() + "," + commodiymap.get(s.getKey()) + "," + s.getValue()+"\r\n");
				}
			coOut.close();
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました。");
		}
	}
}
