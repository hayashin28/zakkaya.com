package ctrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import action.Action;

/***
 *
 * @author hayas
 *
 */
@WebServlet("/FrontController")
public class FrontController extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");

		String action = request.getParameter("action");

		try {
			String className = "action." + action.substring(0, 1).toUpperCase()
					+ action.substring(1) + "Action";

			// 文字列からClass取得
			Class<?> clazz = Class.forName(className);
			// Classによって新しいインスタンス生成
			Action abstractAction = (Action) clazz.newInstance();

			// 遷移方法を決めるためのフラグとして使用する
			StringBuffer sb = new StringBuffer();

			// アクションクラスから遷移先のURLが返される
			String url = abstractAction.execute(request, response, sb);
			if (sb.toString().isEmpty()) {
				request.getRequestDispatcher(url)
						.forward(request, response);
			} else {
				response.sendRedirect(request.getContextPath() + url);
			}
			return;

		} catch (ClassNotFoundException e) {
			// 指定した名称のクラスが存在しなかった場合など
			setException(request, e);
		} catch (NullPointerException e) {
			// Class.forNameでクラスのオブジェクトが取得できなかった場合など
			setException(request, e);
		} catch (InstantiationException e) {
			// インスタンス作成不可の場合（パラメータなしのコンストラクタ存在しない場合 - new Xxxx()でエラーの場合）
			setException(request, e);
		} catch (IllegalAccessException e) {
			// 権限がないときなど
			setException(request, e);
		} catch (Exception e) {
			setException(request, e);
		} finally {/* 処理なし */}

		response.sendRedirect(request.getContextPath() + "/view/err.jsp");
	}

	/**
	 * エラーのスタックトレースを文字列に変換→Sessionに格納する処理
	 * @param request
	 * @param e
	 */
	private void setException(HttpServletRequest request, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		String err = sw.toString();

		HttpSession session = request.getSession();
		session.setAttribute("exception", err);
	}
}
