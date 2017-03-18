#BIGDATAROBO UniVerse データアクセスフレームワーク

##オブジェクトマッピング
UniVerse データアクセスフレームワークはUniVerseデータベースに保存されるレコードをオブジェクトにマッピングするフレームワークです。
UniVerseのテーブルはUniEntityに、フィールドはUniFieldに、アソシエーションはUniAssociationに、複数テーブルのレコードの関連はUniJoinにマッピングされます。

##UniContextの生成
UniContextはすべてのデータベースアクセスの入り口になるクラスです。
UniContextは同一スレッド内からいつでも参照できるように、スレッドローカルに一つ生成されます。
Webアプリケーションの場合はブラウザセッションの初期化を行うタイミングで次のようなコードを実行します。

```
if(UniContext.peek() == null)
	UniContext.bindNewContext();
```

`peek()`メソッドはスレッドローカル内にUniContextのインスタンスが存在するかを調べるメソッドです。`peek()`の呼び出しの結果、UniContextのインスタンスが存在しない場合は、`bindNewContext()`メソッドを呼び出して、スレッドローカル内に新しいUniContextインスタンスを設定します。

UniContextのインスタンスが生成されるときに、UniModel.xmlファイルが読み込まれてUniModelGroupが生成され、その中にはデータベースアクセスに必要なマッピング情報がUniModelとして作られます。

データベースアクセスを行う場合は、以下のコードを記述してUniContextとインスタンスを取得してから各種メソッドを呼び出します。

```
UniContext myContext = UniContext.get(); // UniContextの取得
List objects = myContext.executeQuery(myQuerySpec);　//検索
```

ブラウザセッションの初期化の際に生成したUniContextはそのセッションが終了するタイミングで次のようなコードを記述して消去するようにします。

```
UniContext.unbind();
```

##データベースモデリング
###モデルファイル
UniVerseデータアクセスフレームワーク（2015/08/07時点ではAribaWebアプリケーションのみを想定）はWebServerResource内のuniverseディレクトリに保存されたUniModel.xmlファイルからデータベースとオブジェクトモデルを対応付けたモデル情報を読み込みます。

###モデルファイルの例

```
<?xml version="1.0" encoding="utf-8" ?>

<models>
	<model name="Example">
	
		<entity name="Person" file="PERSON" class="model.Person">
			<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
			<field name="NAME" key="name" valueClass="String" />
			<field name="EMAIL" key="email" valueClass="String" />
			<field name="DISCRIMINATOR" key="discriminator" valueClass="String" />
		</entity>
		
		<entity name="Guest" file="PERSON" class="model.Guest">
			<inheritance type="singleTable" parentEntity="Person" 
				discriminatorField="discriminator" discriminatorValue="Guest" />
			<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
			<field name="NAME" key="name" valueClass="String" />
			<field name="EMAIL" key="email" valueClass="String" />
			<field name="DISCRIMINATOR" key="discriminator" valueClass="String" />
		</entity>
		
		<entity name="Customer" file="CUSTOMER" class="model.Customer">
			<inheritance type="tablePerClass" parentEntity="Person" />
			<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
			<field name="NAME" key="name" valueClass="String" />
			<field name="EMAIL" key="email" valueClass="String" />
			<field name="PHONE" key="phone" valueClass="String" />
			<join key="sales" toMany="true" destinationEntityName="CustomerSales" 
				sourceKey="id" destinationKey="customerId" ownsDestination="true" />
		</entity>
		
		<entity name="CustomerSales" file="CUSTOMER_SALES" class="model.CustomerSales">
			<field name="@ID" key="id" valueClass="Long" primaryKey="true" />
			<field name="DATE" key="date" valueClass="Date" dateFormat="MM/dd/yy" />
			<field name="AMOUNT" key="amount" valueClass="Double" />
			<field name="CUSTOMERID" key="customerId" valueClass="Long" />
			<join key="customer" toMany="false" destinationEntityName="Customer" 
				sourceKey="customerId" destinationKey="id" ownsDestination="false" />
		</entity>
		
		<entity name="Product" file="PRODUCT" class="model.Product">
			<field name="@ID" key="productCode" valueClass="String" primaryKey="true" />
			<field name="NAME" key="productName" valueClass="String" />
			<field name="PRICE" key="price" valueClass="Double" />
			<field name="DESCRIPTION" key="description" valueClass="String" />
			<field name="CATEGORY_ID" key="categoryId" valueClass="Long" />
			<join key="category" toMany="false" destinationEntityName="Category" 
				sourceKey="categoryId" destinationKey="id" ownsDestination="false" />
		</entity>
		
		<entity name="Category" file="CATEGORY" class="model.Category">
			<field name="@ID" key="Id" valueClass="Long" primaryKey="true" />
			<field name="NAME" key="name" valueClass="String" />
			<field name="PARENT_CATEGORY_ID" key="parentCategoryId" valueClass="Long" />
			<join key="subCategories" toMany="true" destinationEntityName="Category" 
				sourceKey="id" destinationKey="parentCategoryId" ownsDestination="false" />
			<join key="parentCategory" toMany="false" destinationEntityName="Category" 
				sourceKey="parentCategoryId" destinationKey="id" ownsDestination="false" />
			<join key="products" toMany="true" destinationEntityName="Product" 
				sourceKey="id" destinationKey="categoryId" ownsDestination="false" />
		</entity>
		
		<connection host="192.168.56.201" port="31438" username="root" password="root" 
			accountPath="EXAMPLE_DB" clientEncoding="UTF8"/>
	</model>
</models>

```

###UniModelGroup

UniModelGroupはUniModel.xmlの<models>要素に対応するクラスです。UniModelGroupはUniModel.xmlで定義されたすべてのモデルを束ねたクラスです。
UniModelGroupのインスタンスはUniContextが生成されるときにUniModel.xmlから読み込んで生成されるので、通常はプログラマーが明示的にこれらのクラスのインスタンスを生成するコードを書く必要はありません。
現在の実装ではプログラム内で使用できるUniModelGroupのインスタンスは一つしかありません。UniModelGroupのインスタンスを取得するには次のようにdefaultGroup()メソッドを呼び出します。

```
UniModelGroup modelGroup = UniModelGroup.defaultModelGroup();
```


###UniModel

UniModelはUniModel.xmlの<model>要素に対応するクラスです。UniModelの中にはUniEntityの集合とデータベース接続情報（UniConnectionInfo）を定義します。
UniModelは名前（name属性）を持ち、UniModelGroupの次のメソッドを使用して特定のUniModelのインスタンスを取得することができます。

```
UniModel aModel = UniModelGroup.modelNamed("Example");
```

###UniEntity

UniEntityはUniModel.xmlの<entity>要素に対応するクラスです。
UniEntityには複数のUniField、UniAssociation、UniJoinを含めることができます。
特定のUniEntityを取得するには次のように記述します。

```
// エンティティ名を使って取得
UniEntity customerEntity = UniModelGroup.defaultModelGroup().entityNamed("Customer");
```

または

```
// エンティティクラスを使って取得
UniEntity customerEntity = UniModelGroup.defaultModelGroup().entityForClass(Customer.class);
```


###UniField

UniFieldはUniModel.xmlの<field>要素に対応するクラスです。この要素にはUniVerse上のフィールド名、マッピングされるオブジェクトのプロパティ名、値タイプ、プライマリキー属性、日付フォーマット、マルチバリュー属性、アソシエーション属性などです。
エンティティの特定のUniFieldを取得するには次のように記述します。

```
UniField nameField = customerEntity.fieldNamed("name");
```


###UniAssociation

UniAssociationはUniModel.xmlの<association>に対応するクラスで、抽象クラスUniRelationshipのサブクラスです。
UniAssociationはUniFieldRefを保持します。UniFieldRefはUniFieldへの参照です。
UniAssociationには対一の関連と対他の関連を表現するものがあります。

エンティティの特定のUniFieldを取得するには次のように記述します。

```
UniAssocation ratingAssoc = customerEntity.associationNamed("ratings");
```


###UniJoin

UniJoinはUniModel.xmlの<join>に対応するクラスで、抽象クラスUniRelationshipのサブクラスです。
UniJoinは関連するエンティティ名、ジョインに必要なキーなどが設定されます。
UniJoinには対一の関連と対他の関連を表現するものがあります。

エンティティの特定のUniFieldを取得するには次のように記述します。

```
UniJoin salesJoin = customerEntity.joinNamed("sales");
```


###UniConnectionInfo

UniConnectionInfoはUniModel.xmlの<connection>要素に対応するクラスです。これにはUniVerseデータベースが稼働するホスト名またはIPアドレス、ログインするためのユーザIDとパスワード、アカウントパス、クライアント文字エンコーディング、セッションタイムアウト時間が設定されます。

##データの検索

データベースからデータを検索するときは、対象のエンティティを取得し、必要であればUniPredicateを使って検索条件を組み立て、UniQuerySpecificationを生成して`executeQuery()`メソッドを呼び出します。

###UniQuerySpecification

UniQuerySpecificaionはデータベース検索を表現するクラスです。
データ検索を行う場合はUniEntityとUniPredicateを引数としてUniQuerySpecificationのインスタンスを生成して`executeQuery()`メソッドを呼び出します。次の例は検索条件を指定せずに全件を読み込むコードです。

```
UniEntity customerEntity = UniModelGroup.defaultModelGroup().entityNamed("Customer");
UniQuerySpecification spec = new UniQuerySpecification(customerEntity, null);
List<Customer> customers = spec.executeQuery(spec);
```

###UniPredicate

UniPredicateは検索条件を表現するクラスです。
KeyValue型はオブジェクトのプロパティ名と値（必要なら演算子）を指定して検索条件を作る場合に使用します。
KeyKey型はオブジェクトのプロパティの関係を指定して検索条件を作る場合に使用します。これらを組み合わせてAND条件、OR条件、Not条件を作ることもできます。

```
// KeyValue型
UniPredicate kv = new UniPredicate.KeyValue("name", "BIGDATAROBO");

UniPredicate kv = new UniPredicate.KeyValue("name", "BIGDATA", UniPredicate.Operator.Contains);

// KeyKey型
UniPredicate kk = new UniPredicate.KeyKey("stockQuantity", "soldQuantity", UniPredicateion.Operator.Gte);

// AND型
UniPredicate and = new UniPredicate.And(predicate1, predicate2, predicate3);

// OR型
UniPredicate or = new UniPredicate.Or(predicate1, predicate2, predicate3);

// Not型
UniPredicate not = new UniPredicate.Not(predicate);

```

またUniVerseのEval()式をそのまま記述するためにEval型のUniPredicateもあります。

```
UniPredicate ev = new UniPredicate.Eval("EVAL \"BALANCE<1,1> - BALANCE<1,13>\" > " + 50000)
```

###UniSortOrdering

UniSortOrderingは検索時のデータの並べ替え方法を指定するクラスです。１つ以上のUniSortOrderingのListを生成してUniQuerySpecificationに設定して使用します。

```
UniEntity customerEntity = UniModelGroup.defaultModelGroup().entityNamed("Customer");
UniQuerySpecification spec = new UniQuerySpecification(customerEntity, null);

List<UniSortOrdering> orderings = new ArrayList<UniSortOrdering>();
orderings.add(new UniSortOrdering("name", UniSortOrdering.Direction.Ascending);
orderings.add(new UniSortOrdering("age", UniSortOrdering.Direction.Descending);

spec.setSortOrderings(orderings);

```

###プライマリキーを指定してデータを読み込む

##データのインサート

UniContextの`create()`メソッドによりデータクラスのインスタンスを生成した後に、そのインスタンスのプロパティーを設定して`saveChanges()`を呼び出します。

```
Customer customer = UniContext.get().create(Customer.class);
// customerのプロパティーを設定する
UniContext.get().saveChanges();
```

プライマリーキーを明示的に設定しない場合は、UniEntityに設定されたUniPrimaryKeyGeneratorを使ってプライマリーキーが設定されますが、UniPrimaryKeyGeneratorの設定がない場合はデフォルトのプライマリキー生成機能（SequenceTableKeyGenerator）が使用されます。
SequenceTableKeyGeneratorは`_SEQUENCE_TABLE`という名前のテーブルをデータベース内に自動的に生成し、エンティティ毎にキーの値を１ずつインクリメントして、新しいレコードのプライマリキーを生成します。


##データのアップデート

```
UniContext.get().updateObject(customer);
// customerのプロパティーの値を変更する
UniContext.get().saveChanges();
```

##データのデリート

```
UniContext.get().deleteObject(customer);
UniContext.get().saveChanges()
```


##継承

###Single Tableマッピング
Single Tableマッピングはクラスの継承関係を単一のテーブルで実現する方式です。クラスの識別にはdiscriminatorフィールドと呼ばれる文字列型のフィールドに格納されたクラス名を使用します。

例えば、抽象的なクラスPersonとその具象サブクラスGuestは次のように実装します。

**Personクラス**

```
package model;

public abstract class Person {

	Long id;
	String name;
	String email;
	String discriminator;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDiscriminator() {
		return discriminator;
	}
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}
}
```

**Guestクラス**

```
package model;

public class Guest extends Person {

}
```

UniModel.xmlでのPersonクラスとGuestクラスの定義は次のようになります。

```
<entity name="Person" file="PERSON" class="model.Person">
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="DISCRIMINATOR" key="discriminator" valueClass="String" />
</entity>
		
<entity name="Guest" file="PERSON" class="model.Guest">
	<inheritance type="singleTable" parentEntity="Person"
		discriminatorField="discriminator" discriminatorValue="Guest" />
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="DISCRIMINATOR" key="discriminator" valueClass="String" />
</entity>

```

###Table Per Classマッピング

Table Per Classマッピングはクラスの継承関係をクラスごとの複数テーブルで実現する方式です。
例えば、抽象的なクラスPersonとその具象サブクラスCustomerは次のように実装します。

**Personクラス**

```
package model;

public abstract class Person {

	Long id;
	String name;
	String email;
	String discriminator;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDiscriminator() {
		return discriminator;
	}
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}
}
```

**Customerクラス**

```
package model;

import java.util.List;

public class Customer extends Person {

	String phone;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

}

```

UniModel.xmlでのPersonクラスとCustomerクラスの定義は次のようになります。

```
<entity name="Person" file="PERSON" class="model.Person">
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="DISCRIMINATOR" key="discriminator" valueClass="String" />
</entity>
		
<entity name="Customer" file="CUSTOMER" class="model.Customer">
	<inheritance type="tablePerClass" parentEntity="Person" />
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="PHONE" key="phone" valueClass="String" />
</entity>

```

Table Per Classマッピングの場合はdiscriminatorフィールドを使用しないので、テーブルにdiscriminatorフィールドを用意する必要も、UniEntityの中にdiscriminatorフィールドを記述する必要もありません。

###継承されたクラスのオブジェクトの検索

抽象クラスPerson、その具象クラスのGuestとCustomerという関係がある場合に、次のようにPersonエンティティの検索を実行すると、具象クラスのGuestとCustomerを検索できます。

```
UniEntity entity = UniModelGroup.defaultGroup().entityForClass("Person");
UniQuerySpecification spec = new UniQuerySpecification(entity, null);
List<Person> = (List<Person>) UniContext.get().executeQuery(spec);
```

具象クラスCustomerだけを検索したい場合は次のように記述します。

```
UniEntity entity = UniModelGroup.defaultGroup().entityForClass("Customer");
UniQuerySpecification spec = new UniQuerySpecification(entity, null);
List<Person> = (List<Person>) UniContext.get().executeQuery(spec);
```

##リレーション

###アソシエーション

アソシエーション指定されたフィールドのセットを特定のクラスのオブジェクトにマッピングします。
次の例ではCustomerクラスのfrom、to、ratingという３つのフィールドがマルチバリューとして定義されており、その３つのフィールドのセットをRatingというクラスのオブジェクトにマッピングしています。

```
<entity name="Customer" file="CUSTOMER" class="model.Customer">
	<inheritance type="tablePerClass" parentEntity="Person" />
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="PHONE" key="phone" valueClass="String" />
	<field name="RATING_FROM" key="from" multiValue="true" valueClass="Date"
		dateFormat="yyyy/MM/dd" association="A1"/>
	<field name="RATING_TO" key="to" multiValue="true" valueClass="Date"
		dateFormat="yyyy/MM/dd" association="A1"/>
	<field name="RATING" key="rating" multiValue="true" valueClass="String"
		association="A1"/>
	<association name="A1" key="ratings" toMany="true" 
		class="model.Rating">
		<field-ref name="RATING_FROM" />
		<field-ref name="RATING_TO" />
		<field-ref name="RATING" />
	</association>
</entity>
		
```

CustomerクラスとRatingクラスの実装は次のようになります。

**Customer.java**

```
package model;

import java.util.List;

public class Customer {
	String id;
	String name;
	String email;
	String phone;
	List<Rating> ratings;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPersonality() {
		return personality;
	}
	public List<Rating> getRatings() {
		return ratings;
	}
	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}

}

```

**Rating.java**

```
package model;

import java.util.Date;

public class CustomerRating {
	Date from;
	Date to;
	String rating;
	
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	public Date getTo() {
		return to;
	}
	public void setTo(Date to) {
		this.to = to;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}

}

```

###ジョイン

リレーショナルデータベースで使用されるような対他と対一のジョインの定義方法です。
次の例ではCustomerクラスはCustomerSalesクラスのオブジェクトのリストを保持しています。

```
<entity name="Customer" file="CUSTOMER" class="model.Customer">
	<inheritance type="tablePerClass" parentEntity="Person" />
	<field name="@ID" key="id" valueClass="Long" primaryKey="true"/>
	<field name="NAME" key="name" valueClass="String" />
	<field name="EMAIL" key="email" valueClass="String" />
	<field name="PHONE" key="phone" valueClass="String" />
	<join key="sales" toMany="true" destinationEntityName="CustomerSales"
		sourceKey="id" destinationKey="customerId" ownsDestination="true" />
</entity>
		
<entity name="CustomerSales" file="SALES" class="model.CustomerSales">
	<field name="@ID" key="id" valueClass="Long" primaryKey="true" />
	<field name="DATE" key="date" valueClass="Date" dateFormat="MM/dd/yy" />
	<field name="AMOUNT" key="amount" valueClass="Double" />
	<field name="CUSTOMERID" key="customerId" valueClass="Long" />
	<join key="customer" toMany="false" destinationEntityName="Customer"
		sourceKey="customerId" destinationKey="id" ownsDestination="false" />
</entity>
```

CustomerクラスとCustomerSalesクラスの実装は次のようになります。

**Customer.java**

```
package model;

import java.util.List;

public class Customer extends Person {

	String phone;
	List<CustomerSales> sales;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<CustomerSales> getSales() {
		if(sales == null)
			sales = ListUtil.list();
		return sales;
	}
	public void setSales(List<CustomerSales> sales) {
		this.sales = sales;
	}
	public void addToSales(CustomerSales sale) {
		UniRelationship.addObjectToBothSidesOfRelationshipWithKey(this, sale, "sales");
	}
	public void removeFromSales(CustomerSales sale) {
		UniRelationship.removeObjectFromBothSidesOfRelationshipWithKey(this, sale, "sales");
	}
	
}

```

**CustomerSales.java**

```
package model;

import java.util.Date;

public class CustomerSales {
	Long id;
	Long customerId;
	Date date;
	Double amount;
	Customer customer;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}

```



##フォールティング

フォールティングとは検索時の反応速度を高めるために、関連するレコードを遅延フェッチする仕組みです。
そのアソシエーションやジョインをプリフェッチしない設定（shouldPrefetch属性がfalseまたはshouldPrefetch属性の指定がない）がされている場合、特定のエンティティを検索したときにアソシエーション先またはジョイン先のオブジェクトをフェッチしません。

###対他リレーションシップのフォールティング

アソシエーションまたはジョインが対他でshouldPrefetch属性がtrueに設定されていない場合、該当するオブジェクトのプロパティーにはUniFaultingList（ArrayListのサブクラス）が設定されます。アプリケーション中でそのプロパティーに対する操作（size()やget()などのメソッド呼び出し）が行われると、フレームワーク側が自動的に対象のオブジェクトをデータベースから読み込みます。

###対一リレーションシップのフォールティング

アソシエーションまたはジョインが対一でshouldPrefetch属性がtrueに設定されていない場合、該当するオブジェクトのプロパティーにはnullが設定されます。アプリケーション中でそのプロパティーにアクセスしたい場合は、対応するアクセッサメソッドの中で明示的にオブジェクトを読み込むコードを次のように記述します。

```
public class Sales {
	...
	...
	public Product getProduct() {
		return (Product)UniFaulting.DefaultImplementation.getStoredValueForRelationshipWithKey(this, "product");
	}
}

```

あるいは次のようにUniFaultingインターフェースを実装して、アクセッサメソッドの中でそのメソッドを呼び出すようにします。

```
public class Sales implements UniFaulting {

	public Object getStoredValueForRelationshipWithKey(String key) {
		return UniFaulting.DefaultImplementation.getStoredValueForRelationshipWithKey(this, key);
	}
	...
	...
	public Product getProduct() {
		return (Product)getStoredValueForRelationshipWithKey("product");
	}

}

```

##レコードロック
未実装

##キャッシュ

UniModel.xml内の<entity>要素のcacheStrategy属性にキャッシュ方式を設定することができます。

###キャッシュなし
エンティティをキャッシュしない設定。
<br/>この設定を行う場合はcacheStrategyに"none"を指定する。
###標準キャッシュ
30秒程度の短時間のキャッシュでエンティティ定義で特段キャッシュのタイプを指定しない場合はこれがデフォルトのキャッシュ方式として設定される。
<br/>この設定を行う場合はcacheStrategyに"normal"を指定する。
###恒久キャッシュ
次回のクエリ実行までキャッシュを残す設定。
<br/>この設定を行う場合はcacheStrategyに"distantfuture"を指定する。
###統計的キャッシュ
エンティティの検索履歴に基づく統計的なキャッシュ設定。
<br/>この設定を行う場合はcacheStrategyに"statistical"を指定する。
##コネクションプール
##プライマリキーの生成
###シーケンステーブルを利用したプライマリキー生成機能
プライマリキー生成機能を特段指定しない場合、フレームワークはシーケンステーブルを使ってプライマリキーの生成を試みます。シーケンステーブルは`_SEQUENCE_TABLE`という名前で自動生成されます。この場合はプライマリキーはLong型でなければなりません。

###プライマリキー生成機能の実装

独自のプライマリキー生成機能を実装するにはUniPrimaryKeyGeneratorのサブクラスを作り、`newPrimaryKeyForEntity(UniEntity entity, UniContext uniContext)`をオーバーライドします。
プライマリキー生成機能はUniModel.xmlの<entity>要素内に次のように記述します。これによりフレームワークがデータのインサートを行う際に自動的にプライマリキーを生成して対象のオブジェクトのプライマリキーに設定します。

```
<entity name="myEntity" ....>
<primary-key-generator class="MyPrimaryKeyGenerator"/>

....

</entity>
```


##BASICコマンドの実行

UniObjectsSessionにはBASICコマンドをコンパイルおよび実行する機能があります。
BASICコマンドを表現するUniObjectBasicクラスはBASICプログラムを記述したテキストまたはBASICプログラムを記述したファイルとプログラム名、プログラム保存パスを持ちます。
例えばBASICプログラムを記述したテキストをUniVerseに送信してコンパイルするには次のように記述します。

```
UniObjectsBasic basic = new UniObjectsBasic();
basic.setProgram(sourceText);
basic.setProgramName("BASICPROG1");
basic.setStorageName("BP");
UniObjectsSession session = UniContext.get().sessionForEntity(myEntity);
session.storeBasic(basic);

```
この記述でBASICプログラムのソースコードがUniVerseに転送されて保存とコンパイルが行われます。コンパイルだけをやり直す場合は次のように記述します。

```
session.compileBasic(basic);
```

次の記述で特定のBASICプログラムがデータベース内に存在するかコンパイル済みかを確認することもできます。

```
boolean stored = session.isBasicStored(basic); // 存在の確認
boolean compiled = session.isBasicCompiled(basic); // コンパイル済みの確認
```

引数なしでBASICプログラムを実行するには次のように記述します。

```
String result = session.executeBasic(basic);
```

引数を必要とするBASICプログラムを実行するには次のように記述します。

```
boolean recompileFlag = false;
String result = session.executeBasic(basic, recompileFlag, "arg1", "arg2");
```

UniQuerySpecificationを使った検索結果（#0のセレクトリスト）を対象にBASICプログラムを実行することもできます。

```
UniQuerySpecification spec = new UniQuerySpecification(myEntity, somePredicate);
String result = session.executeBasic(basic, spec);

または

String result = session.executeBasic(basic, spec, recompileFlag, "arg1", "arg2");
```

また保存されたセレクトリストを対象にBASICプログラムを実行するには次のように記述します。

```
String result = session.executeBasic(basic, "MYSELECTLIST");

または

String result = session.executeBasic(basic, "MYSELECTLIST", recompileFlag, "arg1", "arg2");
```



##保存されたSELECT LISTの実行
###保存されたSelect Listの実行

名前をつけて保存したセレクトリストを呼び出しすには次のように記述します。

```
UniObjectsSession session = UniContext.get().sessionForModel(myModel);
int listNumber = session.getSavedQuery("MYSELECTLIST", 1);
```
この呼び出してセレクトリストの検索結果が1番のセレクトリストに格納されます。

セレクトリストを呼び出した結果に対して検索を行いたい場合は次のように記述します。

```
UniObjectsSession session = UniContext.get().sessionForModel(myModel);
int listNumber = session.getSavedQuery("MYSELECTLIST", 1);
UniQuerySpecification spec = new UniQuerySpecification(myEntity, myPredicate, listNumber);
List<MyObject> objects = UniContext.get().executeQuery(spec);
```

###Union

UniObjectsSessionの`unionQueries()`メソッドを使うのが最も簡単な方法です。実行結果はセレクトリストに格納され、そのセレクトリストの番号が返されます。

```
UniObjectsSession session = UniContext.get().sessionForModel(myModel);
int listNumber = session.unionQueries("LIST1", "LIST2", "LIST3");
```

**保存されたSelect Listの実行**で説明したように、Unionを実行した結果に対してUniQuerySpecificationを使った検索を実行することができます。

###Intersect

UniObjectsSessionの`intersectQueries()`メソッドを使うのが最も簡単な方法です。実行結果はセレクトリストに格納され、そのセレクトリストの番号が返されます。

```
UniObjectsSession session = UniContext.get().sessionForModel(myModel);
int listNumber = session.intersectQueries("LIST1", "LIST2", "LIST3");
```

**保存されたSelect Listの実行**で説明したように、Intersectを実行した結果に対してUniQuerySpecificationを使った検索を実行することができます。


###Diff

UniObjectsSessionの`diffQueries()`メソッドを使うのが最も簡単な方法です。実行結果はセレクトリストに格納され、そのセレクトリストの番号が返されます。

```
UniObjectsSession session = UniContext.get().sessionForModel(myModel);
int listNumber = session.diffQueries("LIST1", "LIST2", "LIST3");
```

**保存されたSelect Listの実行**で説明したように、Diffを実行した結果に対してUniQuerySpecificationを使った検索を実行することができます。


##ロギング

log4jを使ったログ出力機能を利用しています。フレームワークが使用しているロガーは以下のものです。

```
# 主要メソッドの実行ログ
log4j.logger.universe
# 開発者用
log4j.logger.universe.dev
# UniCommand実行ログ
log4j.logger.universe.command
# データベース接続関連
log4j.logger.universe.connection
# テスト用
log4j.logger.universe.test
# スナップショット関連
log4j.logger.universe.snapshot
# キャッシュ関連
log4j.logger.universe.cache
# 性能計測用
log4j.logger.core.perf
# ユーティリティーメッソッド一般
log4j.logger.core.util
# デリゲート用
log4j.logger.core.util.delegate
# セレクタ用
log4j.logger.core.util.selector
```

##サンプルコード

###最も単純な検索
```
List<Customer> getCustomers() {
	List<Customer> customers;
	UniEntity entity = UniModelGroup.defaultGroup().entityForClass(Customer.class);
	UniQuerySpecification spec = new UniQuerySpecification(entity, null);
	customers = (List<Customer>) UniContext.get().executeQuery(spec);
	return customers;
}

```

###UniPredicateを使って検索条件を指定する

```
List<Customer> findCustomers(String str) {
	List<Customer> customers;
	UniEntity entity = UniModelGroup.defaultGroup().entityNamed("Customer");
	// emailにstrが含まれるもの
	UniPredicate predicate = new UniPredicate.KeyValue("email", str, UniPredicate.Operator.Contains);
	UniQuerySpecification spec = new UniQuerySpecification(entity, predicate);
	customers = (List<Customer>) UniContext.get().executeQuery(spec);
	return customers;
}

```

###プロパティの値を指定して検索する

```
Customer findByEmail(String str) {
	Map<String, Object> fieldValues = new HashMap<String, Object>();
	fieldValues.put("email", str);
	return (Customer) UniContext.get().findOne(Customer.class, fieldValues);
}

```

###プライマリキーを指定して検索する

```
Customer findById(Long id) {
	return UniContext.find(Customer.class, id);
}
```

###オブジェクトを追加する

```
void addNewCustomer(String name, String phone, String email) {
	Customer customer = UniContext.get().create(Customer.class);
	customer.setName(name);
	customer.setPhone(phone);
	customer.setEmail(email);
	try {
		UniContext.get().saveChanges();
	} catch (Exception e) {
		e.printStackTrace();
	}
}

```

###オブジェクトを削除する

```
void deleteCustomer(Customer customer) {
	UniContext.get().deleteObject(customer);
	try {
		UniContext.get().saveChanges();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```


###対他ジョイン先のオブジェクトを追加する
```
public class Customer {
	...
	List<CustomerSales> sales;
	
	...
	public void addToSales(CustomerSales sale) {
		UniRelationship.addObjectToBothSidesOfRelationshipWithKey(this, sale, "sales");
	}
	...
}

void addNewSale() {
	Customer customer = fetchCustomer(customerName);
	CustomerSales sales = UniContext.get().create(CustomerSales.class);
	sales.setDate(new Date());
	sales.setAmount(new Double(15000));
	customer.addToSales(sales);
	try {
		UniContext.get().saveChanges();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

###対他ジョイン先のオブジェクトを削除する

```
public class Customer {
	...
	List<CustomerSales> sales;
	
	...
	public void removeFromSales(CustomerSales sale) {
		UniRelationship.removeObjectFromBothSidesOfRelationshipWithKey(this, sale, "sales");
	}
	...
}

void removeSale(CustomerSales aSale) {
	Customer customer = aSales.getCustomer();
	customer.removeFromSales(aSale);
	try {
		UniContext.get().saveChanges();
	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

###BASICプログラムの実行

```
UniObjectsSession getSessionForModel(String modelName) {
	UniModel model = UniModelGroup.modelNamed(modelName);
	UniContext context = UniContext.get();
	return context.sessionForModel(model);
}

void executeBasicProgram(String path, String programName, String source) {
	UniObjectsSession uSession = getSessionForModel("Example");

	UniObjectsBasic basic = new UniObjectsBasic();
	basic.setProgram(source);
	basic.setProgramName(programName);
	basic.setStorageName(path);
		
	boolean programExists = false;
	boolean programCompiled = false;
	
	// BASICプログラムの存在を確認する
	if(uSession.connection().isStored(basic)) {
		programExists = true;
	}
		
	// BASICプログラムを保存してコンパイルする
	try {
		uSession.connection().storeBasic(basic);
	} catch (Exception e) {
		e.printStackTrace();
		return;
	}
		
	// BASICプログラムがコンパイル済みかどうかを確認する
	if(uSession.connection().isCompiled(basic)) {
		programCompiled = true;
	}

	// BASICプログラムを実行する
	String result = null;	
	try {
		result = uSession.executeBasic(basic, true);
	} catch (Exception e) {
		e.printStackTrace();
	}
		
}
```