-- MySQL dump 10.13  Distrib 5.5.25a, for Win64 (x86)
--
-- Host: localhost    Database: core
-- ------------------------------------------------------
-- Server version	5.5.25a

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cheatsheet`
--

DROP TABLE IF EXISTS `cheatsheet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cheatsheet` (
  `cheatSheetId` varchar(64) NOT NULL,
  `moduleId` varchar(64) NOT NULL,
  `createDate` datetime NOT NULL,
  `solution` longtext NOT NULL,
  PRIMARY KEY (`cheatSheetId`,`moduleId`),
  KEY `fk_CheatSheet_Modules1` (`moduleId`),
  CONSTRAINT `fk_CheatSheet_Modules1` FOREIGN KEY (`moduleId`) REFERENCES `modules` (`moduleId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cheatsheet`
--

LOCK TABLES `cheatsheet` WRITE;
/*!40000 ALTER TABLE `cheatsheet` DISABLE KEYS */;
INSERT INTO `cheatsheet` VALUES ('1ed105033900e462b26ca0685b00d98f59efcd93','0dbea4cb5811fff0527184f99bd5034ca9286f11','2012-02-10 10:11:53','Stop the request with a proxy and change the &quot;username&quot; parameter to be equall to &quot;admin&quot;'),('286ac1acdd084193e940e6f56df5457ff05a9fe1','453d22238401e0bf6f1ff5d45996407e98e45b07','2012-02-10 10:11:53','To complete the lesson, the attack string is the following:<br/>&lt;img src=&quot;https://hostname:port/root/grantComplete/csrfLesson?userId=tempId&quot;/&gt;'),('44a6af94f6f7a16cc92d84a936cb5c7825967b47','cd7f70faed73d2457219b951e714ebe5775515d8','2012-02-10 10:11:53','Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;iframe src=&#39;#&#39; onload=&#39;alert(&quot;XSS&quot;)&#39;&gt;&lt;/iframe&gt;'),('5487f2bf98beeb3aea66941ae8257a5e0bec38bd','2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4','2012-02-10 10:11:53','The user Id\'s in this challenge follow a sequence. The Hidden Users ID is \'11\''),('5eccb1b8b1c033bba8ef928089808751cbe6e1f8','94cd2de560d89ef59fc450ecc647ff4d4a55c15d','2012-02-10 10:11:53','To complete this challenge, you must force another user to submit a post request. The easiest way to achieve this is to force the user to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge2&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge2&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the user to visit this attack page.'),('6924e936f811e174f206d5432cf7510a270a18fa','b70a84f159876bb9885b6e0087d22f0a52abbfcf','2012-02-10 10:11:53','Use the login function with usernames like admin, administrator, root, etc to find administrator email accounts. Use the forgotten password functionality to change the password for the email address recovered. Inspect the response of the password reset request to see what the password was reset to. Use this password to login!'),('6afa50948e10466e9a94c7c2b270b3f958e412c6','82e8e9e2941a06852b90c97087309b067aeb2c4c','2012-02-10 10:11:53','The user Id\'s inthis challenge are hashed using MD5. You can google the ID\'s to find their plain text if you have an internet connection to find their plain text. The sequence of ID\'\'s is as follows;<br/>2, 3, 5, 7, 9, 11<br/>The next number in the sequenceis 13. Modify the request with a proxy so that the id is the MD5 of 13 (c51ce410c124a10e0db5e4b97fc2af39)'),('7382ff2f7ee416bf0d37961ec54de32c502351de','a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d','2012-02-10 10:11:53','Base 64 Decode the &quot;checksum&quot; cookie in the request to find it equals &quot;userRole=user&quot;. Change the value of userRole to be administrator instead. The cookies new value should be &quot;dXNlclJvbGU9YWRtaW5pc3RyYXRvcg==&quot; when you replace it.'),('776ef847e16dde4b1d65a476918d2157f62f8c91','5ca9115f3279b9b9f3308eb6a59a4fcd374846d6','2012-02-10 10:11:53','To complete this challenge, you must force an admin to submit a post request. The easiest way to achieve this is to force the admin to visit a custom webpage that submits the post request. This means the webpage needs to be accessable. It can be accessed via a HTTP server, a public Dropbox link, a shared file area. The following is an example webpage that would complete the challenge<br/><br/>&lt;html&gt;<br/>&lt;body&gt;<br/>&lt;form id=&quot;completeChallenge3&quot; action=&quot;https://hostname:port/user/csrfchallengetwo/plusplus&quot; method=&quot;POST&quot; &gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;userid&quot; value=&quot;exampleId&quot; /&gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;csrfToken&quot; value=&quot;anythingExceptNull&quot; /&gt;<br/>&lt;input type=&quot;submit&quot;/&gt;<br/>&lt;/form&gt;<br/>&lt;script&gt;<br/>document.forms[&quot;completeChallenge3&quot;].submit();<br/>&lt;/script&gt;<br/>&lt;/body&gt;<br/>&lt;/html&gt;<br/><br/>The class form function should be used to create an iframe that forces the admin to visit this attack page.'),('82c207a4e07cbfc54faec884be6db0524e74829e','891a0208a95f1791287be721a4b851d4c584880a','2012-02-10 10:11:53','To complete this challenge, move every character five places back to get the following plaintext;<br/>The result key for this lesson is the following string; myLovelyHorseRunningThroughTheFieldWhereAreYouGoingWithYourBigA'),('860e5ed692c956c2ae6c4ba20c95313d9f5b0383','b6432a6b5022cb044e9946315c44ab262ab59e88','2012-02-10 10:11:53','To perform the CSRF correctly use the following attack string;<br/>https://hostname:port/user/redirect?to=https://hostname:port/root/grantComplete/unvalidatedredirectlesson?userid=tempId'),('945b7dcdef1a36ded2ab008422396f8ba51c0630','d4e2c37d8f1298fcaf4edcea7292cb76e9eab09b','2012-02-10 10:11:53','Input is been filtered. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; onmouseup=&quot;alert(&#39;XSS&#39;)&quot;/&gt;'),('97f946ed0bbda4f85e472321a256eacf2293239d','20e755179a5840be5503d42bb3711716235005ea','2012-02-10 10:11:53','To complete this challenge, you can embed the CSRF request in an iframe very easily as follows;<br/>&lt;iframe src=&quot;https://hostname:port/user/csrfchallengeone/plusplus?userid=exampleId&quot;&gt;&lt;/iframe&gt;<br/>Then you need another user to be hit with the attack to mark it as completed.'),('af5959a242047ee87f728b87570a4e9ed9417e5e','544aa22d3dd16a8232b093848a6523b0712b23da','2012-02-10 10:11:53','To complete this challenge, the following attack strings will return all rows from the table:<br/>&#39; || &#39;1&#39; = &#39;1<br/>&#39; OOORRR &#39;1&#39; = &#39;1<br/>The filter in this case does not filter alternative expressions of the boolean OR statement, and it also does not recursively filter OR, so if you enter enough nested OR statements, they will work as well.'),('b8515347017439da4216c6f8d984326eb21652d0','52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a','2012-02-10 10:11:53','The url of the result key is hidden in a div with an ID &quot;hiddenDiv&quot; that can be found in the source HTML of the lesson.'),('b921c6b7dc82648f0a0d07513f3eecb39b3ed064','ca8233e0398ecfa76f9e05a49d49f4a7ba390d07','2012-02-10 10:11:53','The following attack vector will work wonderfully;<br/>&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;'),('ba4e0a2727561c41286aa850b89022c09e088b67','0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e','2012-02-10 10:11:53','Use the password change function to send a functionality request. Stop this request with a proxy, and take the value of the &quot;current&quot; cookie. Base 64 Decode this two times. Modify the value to an administrator username such as &quot;admin&quot;. Encode this two times and change the value of the current cookie to reflect this change. Sign in as the username you set your current cookie\'\'s value to with the new password you set.'),('bb94a8412d7bb95f84c73afa420ca57fbc917912','9533e21e285621a676bec58fc089065dec1f59f5','2012-02-10 10:11:53','Use a proxy to stop the request to complete the lesson. Change the value of the &quot;lessonComplete&quot; cookie to &quot;lessonComplete&quot; to complete the lesson.'),('c0b869ff8a4cd1f388e5e6bdd6525d176175c296','408610f220b4f71f7261207a17055acbffb8a747','2012-02-10 10:11:53','The lesson can be completed with the following attack string<br/>\' OR \'1\' = \'1'),('c0ed3f81fc615f28a39ed2c23555cea074e513f0','0709410108f91314fb6f7721df9b891351eb2fcc','2012-02-10 10:11:53','To complete this challenge, inspect the javascript that executes when the &quot;check&quot; is performed. The encryption key is stored in the &quot;theKey&quot; parameter. The last IF statment in the script checks if the output is equal to the encrypted Result Key.<br/>So the key and ciphertext is stored in the script. You can use this informaiton to decypt the result key manually with the vigenere cipher. You can also modify the javascript to decode the key for you. To do this, make the following changes;<br/> 1) Change the line &quot;input\\_char\\_value += alphabet . indexOf (theKey . charAt (theKey\\_index));&quot; to: <br/>&quot;input\\_char\\_value -= alphabet . indexOf (theKey . charAt (theKey\\_index));&quot;<br/>This inverts the process to decrypt instead of decrypt<br/>2) Add the following line to the end of the script:<br/>$(&quot;#resultDiv&quot;).html(&quot;Decode Result: &quot; + output);'),('d0a0742494656c79767864b2898247df4f37b728','6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342','2012-02-10 10:11:53','Input is been filtered. What is been filtered out is been completly removed. The filter does not act in a recurrive fashion so with enough nested javascript triggers, it can be defeated. To complete this challenge, enter the following attack string;<br/>&lt;input type=&quot;button&quot; oncliconcliconcliconcliconclickkkkk=&quot;alert(&#39;XSS&#39;)&quot;/&gt;'),('d51277769f9452b6508a3a22d9f52bea3b0ff84d','f771a10efb42a79a9dba262fd2be2e44bf40b66d','2012-02-10 10:11:53','To complete this challenge, the following attack string will return all rows from the table:<br/>&#39; &#39; OR &#39;1&#39; = &#39;1<br/>The filter is implemented very poorly for this challenge, as it only removes the first apostraphy in a string, rather than a recursive funciton.'),('e4cb1c92453cf0e6adb5fe0e66abd408cb5b76ea','ac944b716c1ec5603f1d09c68e7a7e6e75b0d601','2012-02-10 10:11:53','A step by step guid is not yet available for this lesson. You will need a tool like <a>Wire Shark</a> and you will need to search for the packet with the result key! The packet is broadcasted with UDP.'),('e7e44ba680b2ab1f6958b1344c9e43931b81164a','5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e','2012-02-10 10:11:53','To complete this challenge, you must craft a second statment to return Mary Martin\'\'s credit card number as the current statement only returns the customerName attribute. The following string will perform this; </br> &#39; UNION ALL SELECT creditCardNumber FROM customers WHERE customerName = &#39;Mary Martin<br/> The filter in this challenge is difficult to get around. But the \'\'UNION\'\' operator is not been filtered. Using the UNION command you are able to return the results of custom statements.'),('f392e5a69475b14fbe5ae17639e174f379c0870e','201ae6f8c55ba3f3b5881806387fbf34b15c30c2','2012-02-10 10:11:53','The lesson is encoded in Base64. Most proxy applicaitons include a decoder for this encoding.');
/*!40000 ALTER TABLE `cheatsheet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `class` (
  `classId` varchar(64) NOT NULL,
  `className` varchar(32) NOT NULL,
  `classYear` varchar(5) NOT NULL,
  PRIMARY KEY (`classId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modules`
--

DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modules` (
  `moduleId` varchar(64) NOT NULL,
  `moduleName` varchar(64) NOT NULL,
  `moduleType` varchar(16) NOT NULL,
  `moduleCategory` varchar(64) DEFAULT NULL,
  `moduleResult` varchar(256) DEFAULT NULL,
  `moduleHash` varchar(256) DEFAULT NULL,
  `moduleStatus` varchar(16) DEFAULT 'open',
  `incrementalRank` int(11) DEFAULT NULL,
  `scoreValue` int(11) NOT NULL DEFAULT '50',
  `scoreBonus` int(11) NOT NULL DEFAULT '5',
  PRIMARY KEY (`moduleId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modules`
--

LOCK TABLES `modules` WRITE;
/*!40000 ALTER TABLE `modules` DISABLE KEYS */;
INSERT INTO `modules` VALUES ('0709410108f91314fb6f7721df9b891351eb2fcc','Insecure Cryptographic Storage Challenge 2','challenge','Insecure Cryptographic Storage','TheVigenereCipherIsAmethodOfEncryptingAlphabeticTextByUsingPoly','h8aa0fdc145fb8089661997214cc0e685e5f86a87f30c2ca641e1dde15b01177','open',175,50,5),('0dbea4cb5811fff0527184f99bd5034ca9286f11','Insecure Direct Object References','lesson','Insecure Direct Object References','59e571b1e59441e76e0c85e5b49','fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100','open',5,50,5),('0e9e650ffca2d1fe516c5d7b0ce5c32de9e53d1e','Session Management Challenge 3','challenge','Session Management','e62008dc47f5eb065229d48963','t193c6634f049bcf65cdcac72269eeac25dbb2a6887bdb38873e57d0ef447bc3','open',165,50,5),('201ae6f8c55ba3f3b5881806387fbf34b15c30c2','Insecure Cryptographic Storage','lesson','Insecure Cryptographic Storage','base64isNotEncryptionBase64isEncodingBase64HidesNothingFromYou','if38ebb58ea2d245fa792709370c00ca655fded295c90ef36f3a6c5146c29ef2','open',35,50,5),('20e755179a5840be5503d42bb3711716235005ea','CSRF 1','challenge','CSRF','7639c952a191d569a0c741843b599604c37e33f9f5d8eb07abf0254635320b07','s74a796e84e25b854906d88f622170c1c06817e72b526b3d1e9a6085f429cf52','open',155,50,5),('2dc909fd89c2b03059b1512e7b54ce5d1aaa4bb4','Insecure Direct Object Reference Challenge 1','challenge','Insecure Direct Object References','dd6301b38b5ad9c54b85d07c087aebec89df8b8c769d4da084a55663e6186742','o9a450a64cc2a196f55878e2bd9a27a72daea0f17017253f87e7ebd98c71c98c','open',95,50,5),('408610f220b4f71f7261207a17055acbffb8a747','SQL Injection','lesson','Injection','3c17f6bf34080979e0cebda5672e989c07ceec9fa4ee7b7c17c9e3ce26bc63e0','e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594','open',75,50,5),('453d22238401e0bf6f1ff5d45996407e98e45b07','Cross Site Request Forgery','lesson','CSRF','666980771c29857b8a84c686751ce7edaae3d6ac0b00a55895926c748453ef71','ed4182af119d97728b2afca6da7cdbe270a9e9dd714065f0f775cd40dc296bc7','open',55,50,5),('52c5394cdedfb2e95b3ad8b92d0d6c9d1370ea9a','Failure to Restrict URL Access','lesson','Failure to Restrict URL Access','f60d1337ac4d35cb67880a3adda79','oed23498d53ad1d965a589e257d8366d74eb52ef955e103c813b592dba0477e3','open',12,50,5),('544aa22d3dd16a8232b093848a6523b0712b23da','SQL Injection 2','challenge','Injection','fd8e9a29dab791197115b58061b215594211e72c1680f1eacc50b0394133a09f','e1e109444bf5d7ae3d67b816538613e64f7d0f51c432a164efc8418513711b0a','open',135,50,5),('5ca9115f3279b9b9f3308eb6a59a4fcd374846d6','CSRF 3','challenge','CSRF','6bdbe1901cbe2e2749f347efb9ec2be820cc9396db236970e384604d2d55b62a','z6b2f5ebbe112dd09a6c430a167415820adc5633256a7b44a7d1e262db105e3c','open',235,50,5),('5dda8dc216bd6a46fccaa4ed45d49404cdc1c82e','SQL Injection 3','challenge','Injection','9815 1547 3214 7569','b7327828a90da59df54b27499c0dc2e875344035e38608fcfb7c1ab8924923f6','open',205,50,5),('6319a2e38cc4b2dc9e6d840e1b81db11ee8e5342','Cross Site Scripting 3','challenge','XSS','6abaf491c9122db375533c04df','ad2628bcc79bf10dd54ee62de148ab44b7bd028009a908ce3f1b4d019886d0e','open',195,50,5),('82e8e9e2941a06852b90c97087309b067aeb2c4c','Insecure Direct Object Reference Challenge 2','challenge','Insecure Direct Object References','1f746b87a4e3628b90b1927de23f6077abdbbb64586d3ac9485625da21921a0f','vc9b78627df2c032ceaf7375df1d847e47ed7abac2a4ce4cb6086646e0f313a4','open',185,50,5),('891a0208a95f1791287be721a4b851d4c584880a','Insecure Cryptographic Storage Challenge 1','challenge','Insecure Cryptographic Storage','mylovelyhorserunningthroughthefieldwhereareyougoingwithyourbiga','x9c408d23e75ec92495e0caf9a544edb2ee8f624249f3e920663edb733f15cd7','open',85,50,5),('94cd2de560d89ef59fc450ecc647ff4d4a55c15d','CSRF 2','challenge','CSRF','45309dbaf8eaf6d1a5f1ecb1bf1b6be368a6542d3da35b9bf0224b88408dc001','z311736498a13604705d608fb3171ebf49bc18753b0ec34b8dff5e4f9147eb5e','open',215,50,5),('9533e21e285621a676bec58fc089065dec1f59f5','Broken Session Management','lesson','Session Management','6594dec9ff7c4e60d9f8945ca0d4','b8c19efd1a7cc64301f239f9b9a7a32410a0808138bbefc98986030f9ea83806','open',10,50,5),('a4bf43f2ba5ced041b03d0b3f9fa3541c520d65d','Session Management Challenge 1','challenge','Session Management','db7b1da5d7a43c7100a6f01bb0c','dfd6bfba1033fa380e378299b6a998c759646bd8aea02511482b8ce5d707f93a','open',105,50,5),('ac944b716c1ec5603f1d09c68e7a7e6e75b0d601','Insufficient Transport Layer Protection','lesson','Insufficient Transport Layer Protection','15e83da388267da584954d4fe5a127be3dff117eaee7a97fcda40e61f3c2868b','ts906dc0c3dbc3eaaaf6da6ea5ddf17fd5bc46c83d26122952ea2f08a544dd32','open',225,50,5),('b6432a6b5022cb044e9946315c44ab262ab59e88','Unvalidated Redirects and Forwards','lesson','Unvalidated Redirects and Forwards','658c43abcf81a61ca5234cfd7a2','f15f2766c971e16e68aa26043e6016a0a7f6879283c873d9476a8e7e94ea736f','open',65,50,5),('b70a84f159876bb9885b6e0087d22f0a52abbfcf','Session Management Challenge 2','challenge','Session Management','4ba31e5ffe29de092fe1950422a','d779e34a54172cbc245300d3bc22937090ebd3769466a501a5e7ac605b9f34b7','open',145,50,5),('ca8233e0398ecfa76f9e05a49d49f4a7ba390d07','Cross Site Scripting','lesson','XSS','ea7b563b2935d8587539d747d','zf8ed52591579339e590e0726c7b24009f3ac54cdff1b81a65db1688d86efb3a','open',15,50,5),('cd7f70faed73d2457219b951e714ebe5775515d8','Cross Site Scripting 1','challenge','XSS','445d0db4a8fc5d4acb164d022b','d72ca2694422af2e6b3c5d90e4c11e7b4575a7bc12ee6d0a384ac2469449e8fa','open',25,50,5),('d4e2c37d8f1298fcaf4edcea7292cb76e9eab09b','Cross Site Scripting 2','challenge','XSS','495ab8cc7fe9532c6a75d378de','t227357536888e807ff0f0eff751d6034bafe48954575c3a6563cb47a85b1e888','open',115,50,5),('f771a10efb42a79a9dba262fd2be2e44bf40b66d','SQL Injection 1','challenge','Injection','f62abebf5658a6a44c5c9babc7865110c62f5ecd9d0a7052db48c4dbee0200e3','ffd39cb26727f34cbf9fce3e82b9d703404e99cdef54d2aa745f497abe070b','open',125,50,5);
/*!40000 ALTER TABLE `modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `results`
--

DROP TABLE IF EXISTS `results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `results` (
  `userId` varchar(64) NOT NULL,
  `moduleId` varchar(64) NOT NULL,
  `startTime` datetime NOT NULL,
  `finishTime` datetime DEFAULT NULL,
  `csrfCount` int(11) DEFAULT '0',
  `resultSubmission` longtext,
  `knowledgeBefore` int(11) DEFAULT NULL,
  `knowledgeAfter` int(11) DEFAULT NULL,
  `difficulty` int(11) DEFAULT NULL,
  PRIMARY KEY (`userId`,`moduleId`),
  KEY `fk_Results_Modules1` (`moduleId`),
  CONSTRAINT `fk_Results_users1` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Results_Modules1` FOREIGN KEY (`moduleId`) REFERENCES `modules` (`moduleId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `results`
--

LOCK TABLES `results` WRITE;
/*!40000 ALTER TABLE `results` DISABLE KEYS */;
/*!40000 ALTER TABLE `results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `tableName` varchar(32) NOT NULL,
  `currVal` bigint(20) NOT NULL DEFAULT '282475249',
  PRIMARY KEY (`tableName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES ('cheatSheet',282475299),('class',282475249),('modules',282475576),('users',282475250);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `userId` varchar(64) NOT NULL,
  `classId` varchar(64) DEFAULT NULL,
  `userName` varchar(32) NOT NULL,
  `userPass` varchar(512) NOT NULL,
  `userRole` varchar(32) NOT NULL,
  `badLoginCount` int(11) NOT NULL DEFAULT '0',
  `suspendedUntil` datetime NOT NULL DEFAULT '1000-01-01 00:00:00',
  `userAddress` varchar(128) DEFAULT NULL,
  `tempPassword` tinyint(1) DEFAULT '0',
  `userScore` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`userId`),
  UNIQUE KEY `userName_UNIQUE` (`userName`),
  KEY `classId` (`classId`),
  CONSTRAINT `classId` FOREIGN KEY (`classId`) REFERENCES `class` (`classId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('c7ad33304313f87dd2f149aa2c7bd301514dfe52',NULL,'admin','b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86','admin',0,'1000-01-01 00:00:00','admin@securityShepherd.org',1,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-08-23 21:21:15
