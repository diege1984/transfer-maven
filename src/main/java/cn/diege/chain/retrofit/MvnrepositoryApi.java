package cn.diege.chain.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 
 * @author 0092397
 *
 */
public interface MvnrepositoryApi {

	@GET("artifact/{group}")
	public Call<ResponseBody> group(@Path("group") String group);

	@GET("artifact/{group}/{artifact}")
	public Call<ResponseBody> artifact(@Path("group") String group, @Path("artifact") String artifact);

	@GET("artifact/{group}/{artifact}/{version}")
	public Call<ResponseBody> version(@Path("group") String group, @Path("artifact") String artifact,
			@Path("version") String version);

	@GET("search")
	public Call<ResponseBody> search(@Query("q")String q);
}
