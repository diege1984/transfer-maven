package cn.diege.chain;

import static cn.diege.Utils.matchVersion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.diege.MavenMeta;
import cn.diege.chain.retrofit.MvnrepositoryApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

public class NetFinder extends AbstractFinder {

	private MvnrepositoryApi api;

	ExecutorService exec = Executors.newFixedThreadPool(5);

	{
		Retrofit Retrofit = buildRetrofit();
		api = Retrofit.create(MvnrepositoryApi.class);
	}

	@Override
	public void findCurrent(List<String> jarList) {
		Iterator<String> it = jarList.iterator();
		Set<MavenMeta> set = new TreeSet<>();
		while (it.hasNext()) {
			String jarPath = it.next();
			find(jarPath, set);
		}
		exec.shutdown();
		while (true) {
			if (exec.isTerminated()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		it = jarList.iterator();
		while (it.hasNext()) {
			String jarPath = it.next();
			for(MavenMeta meta : set){
				if(jarPath.equals(meta.filePath)){
					it.remove();
					break;
				}
			}
		}
	}

	private void find(String jarPath, Set<MavenMeta> set) {
		File file = new File(jarPath);
		if (!file.exists()) {
			return;
		}
		String fileName = file.getName().replace(".jar", "");
		String version = matchVersion(fileName);

		if (version == null) {
			return;
		}
		fileName = fileName.replace(version, "");
		if (fileName.endsWith("-") || fileName.endsWith(".")) {
			fileName = fileName.substring(0, fileName.length() - 1);
		}

		final List<MavenMeta> list = buildMetas(fileName, version);

		exec.submit(() -> {
			list.forEach(meta -> {
				boolean result = version(meta);
				if (!result) {
					result = query(meta);
				}
				if(result){
					meta.filePath = jarPath;
					set.add(meta);
				}
			});
		});
		//防止被系统误认攻击
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
		}

	}

	private List<MavenMeta> buildMetas(String fileName, String version) {
		String[] strs = fileName.split("-");
		List<MavenMeta> list = new ArrayList<>();
		if (strs.length == 1) {
			list.add(new MavenMeta(strs[0], strs[0], version));
		} else if (strs.length == 2) {
			list.add(new MavenMeta(strs[0], strs[1], version));
			list.add(new MavenMeta(strs[0] + "-" + strs[1], strs[0] + "-" + strs[1], version));
		} else if (strs.length == 3) {
			list.add(new MavenMeta(strs[0] + "-" + strs[1], strs[2], version));
			list.add(new MavenMeta(strs[0], strs[1] + "-" + strs[2], version));
		} else if (strs.length == 4) {
			list.add(new MavenMeta(strs[0] + "-" + strs[1] + "-" + strs[2], strs[3], version));
			list.add(new MavenMeta(strs[0] + "-" + strs[1], strs[2] + "-" + strs[3], version));
			list.add(new MavenMeta(strs[0], strs[1] + "-" + strs[2] + "-" + strs[3], version));
		}
		return list;
	}

	private boolean version(MavenMeta meta) {
		try {
			Call<ResponseBody> call = api.version(meta.groupId, meta.artifactId, meta.version);
			retrofit2.Response<ResponseBody> response = call.execute();
			System.out.println(String.format("groupId=%s,artifactId=%s,version=%s", meta.groupId, meta.artifactId, meta.version));
			if (response.code() == 200) {
				System.out.println("正确找到");
				return true;
			}
			if (response.code() == 403) {
				System.out.println("已被网站拒绝访问");
			}
			System.out.println("未找到");
		} catch (Exception e) {
		}
		return false;
	}

	private boolean query(MavenMeta meta) {
		try {
			Call<ResponseBody> call = api.search(meta.groupId);
			retrofit2.Response<ResponseBody> response = call.execute();
			if (response.code() == 200) {
				String body = response.body().string();
				String url = findUrl(body);
				String[] parts = url.split("/");
				meta.groupId = parts[2];
				meta.artifactId = parts[3];
				return version(meta);
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * 构建Retrofit对象
	 * 
	 * @param version
	 * @return
	 */
	public static Retrofit buildRetrofit() {
		OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
		// 添加拦截器
		okHttpClient.addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				Request original = chain.request();
				Request request = original.newBuilder().method(original.method(), original.body()).build();
				return chain.proceed(request);
			}
		});

		HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
		// 设定日志级别
		httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf("NONE"));
		// 添加拦截器
		okHttpClient.addInterceptor(httpLoggingInterceptor);
		okHttpClient.connectTimeout(3, TimeUnit.SECONDS);
		okHttpClient.readTimeout(3, TimeUnit.SECONDS);
		okHttpClient.writeTimeout(3, TimeUnit.SECONDS);

		Retrofit retrofit = new Retrofit.Builder().baseUrl("https://mvnrepository.com").client(okHttpClient.build())
				.build();
		return retrofit;
	}

	public static void main(String[] args) throws IOException {
		Retrofit Retrofit = buildRetrofit();
		Call<ResponseBody> call = Retrofit.create(MvnrepositoryApi.class).search("aopalliance");
		retrofit2.Response<ResponseBody> response = call.execute();
		if (response.code() == 200) {
			String body = response.body().string();
			findUrl(body);
		}
	}

	public static String findUrl(String body) {
		Pattern p = Pattern.compile("\"/artifact/.*?/.*?\"");
		Matcher m = p.matcher(body);
		if (m.find()) {
			return m.group().replaceAll("\"", "");
		}
		return null;
	}

}
