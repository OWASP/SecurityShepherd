<%@page contentType="text/html" pageEncoding="UTF-8"%>

			<form style="position:absolute;top: 0.5em;right: 0.5em">
				<select id="lang" name="lang" onchange="submit()">
					<option value="en" ${lang == 'en' ? 'selected' : ''}>English</option>
					<option value="es" ${lang == 'es' ? 'selected' : ''}>Español</option>
					<option value="fr" ${lang == 'fr' ? 'selected' : ''}>Français</option>
					<option value="pt" ${lang == 'pt' ? 'selected' : ''}>Português</option>
					<option value="ga" ${lang == 'ga' ? 'selected' : ''}>Gaeilge</option>
					<option value="zh" ${lang == 'zh' ? 'selected' : ''}>中文</option>
					<option value="hi" ${lang == 'hi' ? 'selected' : ''}>Hindi(Hinglish)</option>
				</select>
			</form>
