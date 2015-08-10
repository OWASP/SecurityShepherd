<%@page contentType="text/html" pageEncoding="UTF-8"%>

			<form style="position:absolute;top: 0.5em;right: 0.5em">
				<select id="language" name="language" onchange="submit()">
					<option value="en" ${language == 'en' ? 'selected' : ''}>English</option>
					<option value="es" ${language == 'es' ? 'selected' : ''}>Español</option>					
					<option value="ga" ${language == 'ga' ? 'selected' : ''}>Gaeilge</option>
					<option value="zh" ${language == 'zh' ? 'selected' : ''}>中文</option>
				</select>
			</form>
