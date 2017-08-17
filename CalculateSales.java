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
class CalculateSales {

	public static void main(String[]args){
		HashMap<String,String>branchmap = new HashMap<String,String>();
		HashMap<String,String>commodiymap = new HashMap<String,String>();
		HashMap<String,Long> brEarmap = new HashMap<String,Long>();
		HashMap<String,Long> coEarmap = new HashMap<String,Long>();

		ArrayList<File> rcdfiles = new ArrayList<File>();
		ArrayList<Long> numberlist = new ArrayList<Long>();

		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//【branch.lst（支店定義ファイル）の読み込み】
		if(!inPutfile(args[0],"branch.lst","支店","^\\d{3}$",branchmap,brEarmap)){
			return;
		}
		//【commodiy.lst（商品定義ファイル）の読み込み】
		if(!inPutfile(args[0],"commodity.lst","商品","^[0-9a-zA-Z]{8}$",commodiymap,coEarmap)){
			return;
		}

		//inputディレクトリ読み込み→rcdファイルをリスト化
		File dir = new File(args[0]);
		File[] inputdir = dir.listFiles();
		for(int i =0; i<inputdir.length;i++){
			if(inputdir[i].getName().matches("^\\d{8}.rcd$")&&inputdir[i].isFile()){
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
				if(numberlist.get(i)- numberlist.get(ii) != 1){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}
		}
		BufferedReader br = null;
		//【rcdのファイルの読み込み（支店・商品・売上）】
		for(int i = 0; i<rcdfiles.size(); i++){
			try{
				ArrayList<String> rcdlists = new ArrayList<String>();
				File rcd = rcdfiles.get(i);
				FileReader fr = new FileReader(rcd);
				br =new BufferedReader(fr);
				String s;
				while((s=br.readLine()) !=null){
					//rcdの中身をリストに追加する
					rcdlists.add(s);
				}

				File r = rcdfiles.get(i);
				if(rcdlists.size() != 3 ){
					System.out.println(r.getName()+"のフォーマットが不正です");
					return;
				}
				if(!branchmap.containsKey(rcdlists.get(0))){
					System.out.println(r.getName()+"の支店コードが不正です");
					return;
				}
				if(!commodiymap.containsKey(rcdlists.get(1))){
					System.out.println(r.getName()+"の商品コードが不正です");
					return;
				}

				if(!rcdlists.get(2).matches("\\d+")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

				//支店の売上をマップに置く（集計）
				Long brEar = Long.parseLong(rcdlists.get(2));
				Long brTotal = brEarmap.get(rcdlists.get(0)) + brEar;
				brEarmap.put(rcdlists.get(0), brTotal);
				String brvaluse = Long.toString(brEarmap.get(rcdlists.get(0)));
				if(brvaluse.length() > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//商品別の売上をマップに置く（集計）
				Long coTotal = coEarmap.get(rcdlists.get(1)) + brEar;
				coEarmap.put(rcdlists.get(1), coTotal);
				String covaluse = Long.toString(coEarmap.get(rcdlists.get(1)));
				if(covaluse.length() > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}catch(NumberFormatException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally{
				if(br != null)
					try{
						br.close();
					}catch(IOException e){
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
			}
		}

		//メソッド分け
		//【ファイル書き込み】
		if(!outPutfile(args[0],"branch.out",branchmap,brEarmap)){
			return;
		}
		if(!outPutfile(args[0],"commodity.out",commodiymap,coEarmap)){
			return;
		}
	}

	public static boolean outPutfile(
			String dirPath,
			String fileName,
			HashMap<String,String> nameMap,
			HashMap<String,Long> earningsMap
			){
		List<Map.Entry<String,Long>> coentries =
				new ArrayList<Map.Entry<String,Long>>(earningsMap.entrySet());
			Collections.sort(coentries, new Comparator<Map.Entry<String,Long>>() {

			@Override
				public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		BufferedWriter bw = null;
		try{
			File file = new File(dirPath,fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(Entry<String,Long> s :coentries){
				bw.write(s.getKey() + "," + nameMap.get(s.getKey())
				+ "," + s.getValue()+System.getProperty("line.separator"));
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if(bw !=null)
				try{
					bw.close();
				}catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		return true;
	}

	public static boolean inPutfile(
			String dirPath,
			String fileName,
			String lstFile,
			String numberDigits,
			HashMap<String,String> nameMap,
			HashMap<String,Long> earningsMap
			){
		BufferedReader br = null;
		try{
			File file = new File(dirPath,fileName);
			if(!file.exists()){
				System.out.println(lstFile+"定義ファイルが存在しません");
				return false;
			}
			br = new BufferedReader (new FileReader(file));
			String b;
			while((b = br.readLine()) !=null){
				//一行ずつ","で分割する
				String[]read = b.split(",");
				if(read.length != 2 || !read[0].matches(numberDigits)){
					System.out.println(lstFile+"定義ファイルのフォーマットが不正です");
					return false;
				}//支店コードマップ
				nameMap.put(read[0],read[1]);
				//支店の売上マップ
				earningsMap.put(read[0], 0L);
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if(br !=null)
				try{
					br.close();
				}catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
		}
		return true;
	}


}
